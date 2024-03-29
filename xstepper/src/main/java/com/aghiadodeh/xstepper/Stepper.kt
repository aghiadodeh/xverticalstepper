package com.aghiadodeh.xstepper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RelativeLayout
import androidx.core.view.children
import com.aghiadodeh.xstepper.interfaces.IStepper
import com.aghiadodeh.xstepper.models.StepModel
import com.aghiadodeh.xstepper.utils.Animations
import com.aghiadodeh.xstepper.utils.Variables
import com.aghiadodeh.xstepper.databinding.StepperLayoutBinding

@SuppressLint("InflateParams")
class Stepper(context: Context, private val attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
    private val binding: StepperLayoutBinding = StepperLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private var iStepper: IStepper? = null
    private var stepModels = ArrayList<StepModel>()
    private var stepViews = ArrayList<StepView>()
    private var primaryColor = Color.GRAY
    private var defaultStepIndex = -1
    var activeStep = 0
    var previousStep = 0

    init {
        init(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initStepViews()
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context) {
        // Load the styled attributes and set their properties
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.StepperView, 0, 0)
        primaryColor = attributes.getColor(R.styleable.StepperView_stepper_primary_color, Color.GRAY)
        defaultStepIndex = attributes.getInt(R.styleable.StepperView_stepper_opened_step_index, 0)
        Variables.primaryColor = primaryColor
        attributes.recycle()
    }

    fun setListener(iStepper: IStepper) {
        this.iStepper = iStepper
    }

    private fun initStepViews() {
        for (x in 0 until childCount) {
            val child = children.elementAt(x)
            if (child is StepContainer) {
                val stepHeader = child.children.find { it is StepHeader }?.also { view -> child.removeView(view) }
                val stepModel = StepModel(stepNumber = children.indexOf(child), view = child, header = stepHeader as StepHeader?)
                stepModels.add(stepModel)
            }
        }

        stepModels.forEach {
            removeView(it.view)
            val stepView = StepView(context).apply {
                layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                init(stepModel = it, iStepView = iStepView)
            }
            stepViews.add(stepView)
        }

        stepViews.forEach { binding.stepsParent.addView(it) }

        if (defaultStepIndex in 0 until stepViews.size) {
            goToStep(index = defaultStepIndex, firstAction = true)
        } else {
            for (x in 0 until stepViews.size) disableStep(index = x, animate = false)
        }
    }

    fun goToNextStep() {
        if (activeStep <= stepViews.size - 1) { // if active step is not last step
            for (x in activeStep + 1 until stepModels.size) { // get first view with (appear == true)
                if (stepModels[x].appear) {
                    goToStep(index = x, allowed = true)
                    break
                }
            }
        }
    }

    fun goToStep(index: Int, allowed: Boolean = false, firstAction: Boolean = false) {
        val boolean = activeStep > index || firstAction || !stepModels[activeStep].view.needValidation || allowed
        if (boolean) {
            previousStep = activeStep
            activeStep = index
            for (x in 0 until stepViews.size)
                if (x == index) enableStep(index)
                else disableStep(x, !firstAction)
            if (index == stepViews.size - 1) iStepper?.onFinished()
        }
    }

    /*fun stepCompleted(index: Int = activeStep) {
        stepModels[index].completed = true
    }*/

    private fun disableStep(index: Int, animate: Boolean = true) {
        if (animate)
            Animations.slideUp(stepViews[index].binding.stepBody.root)
        else
            stepViews[index].binding.stepBody.root.visibility = GONE

        stepViews[index].stepClosed()
    }

    private fun enableStep(index: Int) {
        Animations.slideDown(stepViews[index].binding.stepBody.root)
        stepViews[index].stepOpened()
        iStepper?.onStepOpening(index)
    }

    fun removeStep(index: Int) {
        stepModels[index].appear = false
        stepViews[index].binding.root.visibility = View.GONE
        reOrderSteps()
    }

    fun restoreStep(index: Int) {
        stepModels[index].appear = true
        stepViews[index].binding.root.visibility = View.VISIBLE
        reOrderSteps()
    }

    @SuppressLint("SetTextI18n")
    private fun reOrderSteps() {
        var stepNumber = 0
        for (x in 0 until stepModels.size) {
            if (stepModels[x].appear) {
                stepViews[x].binding.stepHeader.stepNumber.text = "${stepNumber + 1}"
                stepModels[x].stepNumber = stepNumber++
            }
        }
    }

    private val iStepView = StepView.IStepView { stepModel ->
        val index = stepModels.indexOf(stepModel)
        if (index < activeStep || isPreviousStepsCompleted(index)) goToStep(index)
        else iStepper?.onWaitingForOpen(index) // check if current step is completed before go to other step
    }

    fun isPreviousStepsCompleted(index: Int): Boolean {
        var boolean = true
        for (x in index downTo 0) {
            val model = stepModels[x]
            if (model.view.needValidation && model.appear) {
                boolean = false
                break
            }
        }
        return boolean
    }
}