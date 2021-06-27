package com.saif.numberslidingpickerview

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.view.setPadding
import kotlin.math.*

class NumberSlidingPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs)
{
    var minNum = 0
    var maxNum = 100
    var initNum = 50//minNum
    var currentNum = initNum
    var stepNum = 1
    var orientation = Orientation.Horizontal
    var unitText: String? = null
    set(value) {
        if (value == null) unitTopMargin = 0f
        field = value
    }
    var incDecLength = convertDPtoPX(32)
    /** if (orientation = Vertical) Top Margins only
     *  else if (orientation = Horizontal) Right and Left Margins
     * */
    var numMargin: Float = convertDPtoPX(12).toFloat()
    /** if (orientation = Vertical) Top and Bottom Margins
     * else if (orientation = Horizontal) Top only
     * */
    var unitTopMargin: Float = convertDPtoPX(8).toFloat()
    /*  This Attributes works only in Vertical Orientation */
    var unitBottomMargins: Float = convertDPtoPX(8).toFloat()
    var unitTextSize: Float = convertSPToPX(18f)
    var numTextSize: Float = convertSPToPX(28f)
    var numColor = Color.RED
    var unitColor = Color.BLUE
    var incDecColor = Color.BLACK

    ////    //////////////
    private val paintUnit by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val paintNum by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val paintIncDec by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val rightPath by lazy { Path() }
    private val leftPath by lazy { Path() }
    private val rectNumBounds = Rect()
    private val rectUnitTextBounds = Rect()

    private var isTouchInc = false
    private var isTouchDec = false
    private val counterHandler by lazy { Handler() }
    private val MAX_SPEED = 5
    private val MIN_SPEED = 1
    /** this value must be from 1 to 5 where is 1 is the MIN speed*/
    private var incDecSpeed = MAX_SPEED * 200
        set(value) {
            if (value in MIN_SPEED..MAX_SPEED)
                field = convertSpeedToDelay(value) * 200
        }
    private var lastCor = 0f
    private var corDiff = 40f
    ////////////////////////////////*******//////////////////////////////////////////

    init {
        attrs?.let {
            val attrArray = context.theme?.obtainStyledAttributes(it, R.styleable.NumberSlidingPickerView, 0, 0)
            try
            {
                attrArray?.let {
                    minNum = it.getInteger(R.styleable.NumberSlidingPickerView_minNum, 0)
                    maxNum = it.getInteger(R.styleable.NumberSlidingPickerView_maxNum, 100)
                    initNum = it.getInteger(R.styleable.NumberSlidingPickerView_initNum, 0)
                    stepNum = it.getInteger(R.styleable.NumberSlidingPickerView_stepNum, 1)
                    it.getInteger(R.styleable.NumberSlidingPickerView_orientation, Orientation.Horizontal.value).run {
                        orientation = if (this == Orientation.Horizontal.value)
                            Orientation.Horizontal else Orientation.Vertical
                    }
                    unitText = it.getString(R.styleable.NumberSlidingPickerView_unitText)
                    numMargin = it.getDimension(R.styleable.NumberSlidingPickerView_numMargin, numMargin)
                    unitTopMargin = it.getDimension(R.styleable.NumberSlidingPickerView_unitTopMargin, unitTopMargin)
                    unitBottomMargins = it.getDimension(R.styleable.NumberSlidingPickerView_unitBottomMargins, unitBottomMargins)
                    incDecLength = it.getDimensionPixelOffset(R.styleable.NumberSlidingPickerView_incDecLength,  incDecLength)
                    unitTextSize = it.getDimension(R.styleable.NumberSlidingPickerView_unitTextSize, unitTextSize)
                    numTextSize = it.getDimension(R.styleable.NumberSlidingPickerView_numTextSize, numTextSize)
                    numColor = it.getColor(R.styleable.NumberSlidingPickerView_numColor, numColor)
                    unitColor = it.getColor(R.styleable.NumberSlidingPickerView_unitColor, unitColor)
                    incDecColor = it.getColor(R.styleable.NumberSlidingPickerView_incDecColor, incDecColor)
                }
            }
            finally {
                attrArray?.recycle()
            }
        }

        init()
    }

    private fun init()
    {
        with(paintNum)
        {
            color = numColor
            style = Paint.Style.FILL
            textSize = numTextSize
            textAlign = Paint.Align.CENTER
        }

        with(paintUnit)
        {
            color = unitColor
            style = Paint.Style.FILL
            textSize = this@NumberSlidingPickerView.unitTextSize
            textAlign = Paint.Align.CENTER
        }

        with(paintIncDec)
        {
            color = incDecColor
            style = Paint.Style.FILL
        }

        setPadding(18)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        val num = if (maxNum == -1) currentNum else maxNum
        paintNum.getTextBounds(num.toString(), 0, num.toString().length, rectNumBounds)
        val numWidth = rectNumBounds.width()
        val numHeight = rectNumBounds.height()

        var unitTopMarginTemp = 0f
        var unitBottomMarginTemp = 0f
        unitText?.let {
            paintUnit.getTextBounds(it, 0, it.length, rectUnitTextBounds)
            unitTopMarginTemp = unitTopMargin
            unitBottomMarginTemp = unitBottomMargins
        }
        val unitTextHeight = rectUnitTextBounds.height()
        val unitTextWidth = rectUnitTextBounds.width()

        val desiredWidth: Int
        val desiredHeight: Int

        if (orientation == Orientation.Horizontal)
        {
            desiredWidth = (
                    (widthOfTriangle(Orientation.Horizontal) * 2) + max(unitTextWidth, numWidth) + (numMargin * 2) + paddingStart + paddingEnd)
                    .roundToInt()
            desiredHeight = (
                    max(heightOfTriangle(Orientation.Horizontal), numHeight.toFloat()) + unitTextHeight + unitTopMarginTemp + paddingTop + paddingBottom
                    ).roundToInt()
        }
        else
        {
            desiredWidth = max(max(widthOfTriangle(Orientation.Vertical), numWidth.toFloat()), unitTextWidth.toFloat()).roundToInt() + paddingStart + paddingEnd
            desiredHeight = ( (heightOfTriangle(Orientation.Vertical) * 2) + numHeight + unitTextHeight + numMargin + unitBottomMarginTemp + unitTopMarginTemp + paddingTop + paddingBottom
                    ).roundToInt()

            Log.d("saif", "numHeight= $numHeight,   unitTextHeight= $unitTextHeight,   numMargin= $numMargin,   unitMarginTemp= $unitTopMarginTemp")
        }

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (MeasureSpec.getMode(widthMeasureSpec))
        {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(widthSize, desiredWidth)
            else -> desiredWidth
        }

        Log.d("saif", "heightMode= ${MeasureSpec.getMode(heightMeasureSpec)}")
        val height = when(MeasureSpec.getMode(heightMeasureSpec))
        {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(heightSize, desiredHeight)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        super.onSizeChanged(w, h, oldw, oldh)

        rightPath.reset()
        leftPath.reset()

        when (orientation) {
            Orientation.Horizontal ->
            {
                val headTriangleY = height /2f //max(rectNumBounds.height(), incDecLength) /2f
                val widthOfTriangle = widthOfTriangle(Orientation.Horizontal)

                with(leftPath)
                {
                    moveTo(paddingStart.toFloat(), headTriangleY)
                    lineTo(widthOfTriangle + paddingStart, headTriangleY - (incDecLength/2))
                    lineTo(widthOfTriangle + paddingStart, headTriangleY + (incDecLength/2))
                    close()
                }
                with(rightPath)
                {
                    moveTo(width.toFloat() - paddingEnd, headTriangleY)
                    lineTo(width - widthOfTriangle - paddingEnd, headTriangleY - (incDecLength/2))
                    lineTo(width - widthOfTriangle - paddingEnd, headTriangleY + (incDecLength/2))
                    close()
                }
            }
            Orientation.Vertical -> {
                with(rightPath)
                {
                    moveTo(width/2f, paddingTop.toFloat())
                    lineTo((width/2f) + (incDecLength/2), heightOfTriangle(Orientation.Vertical) + paddingTop)
                    lineTo((width/2f) - (incDecLength/2), heightOfTriangle(Orientation.Vertical) + paddingTop)
                    close()
                }
                with(leftPath)
                {
                    moveTo(width/2f, height.toFloat())
                    lineTo((width/2f) + (incDecLength/2), height - heightOfTriangle(Orientation.Vertical) - paddingBottom)
                    lineTo((width/2f) - (incDecLength/2), height - heightOfTriangle(Orientation.Vertical) - paddingBottom)
                    close()
                }
            }
        }

    }

    override fun onDraw(canvas: Canvas?)
    {
        if (canvas == null)
            return

        // draw Rect Shape of Inc And Dec Counter;
        if (orientation == Orientation.Horizontal)
        {
            drawInHorizontal(canvas)
        }
        else
        {
            drawInVertical(canvas)
        }
    }

    private fun drawInHorizontal(canvas: Canvas)
    {
        canvas.drawPath(leftPath, paintIncDec)

        val currentX = (width/2f)
        val whiteSpaceY = (height - (rectNumBounds.height() + rectUnitTextBounds.height() + unitTopMargin + paddingTop + paddingBottom)) / 2
        var currentY = whiteSpaceY + paddingTop+ if (rectNumBounds.height() > heightOfTriangle(Orientation.Horizontal))
            rectNumBounds.height().toFloat()
        else ((incDecLength/2f) + (rectNumBounds.height()/2f))

        canvas.drawText(currentNum.toString(), currentX, currentY, paintNum)

        unitText?.let {
            currentY += rectUnitTextBounds.height() + unitTopMargin
            canvas.drawText(it, currentX, currentY, paintUnit)
        }

        canvas.drawPath(rightPath, paintIncDec)
    }

    private fun drawInVertical(canvas: Canvas)
    {
        canvas.drawPath(leftPath, paintIncDec)

        val currentX = width / 2f
        val whiteSpaceY = (height - (heightOfTriangle(Orientation.Vertical)*2 + rectNumBounds.height() + rectUnitTextBounds.height() + unitTopMargin+ unitBottomMargins + paddingTop + paddingBottom)) / 2
        var currentY = whiteSpaceY + heightOfTriangle(Orientation.Vertical) + numMargin + rectNumBounds.height()

        canvas.drawText(currentNum.toString(), currentX , currentY, paintNum)

        unitText?.let {
            currentY += unitBottomMargins + (rectUnitTextBounds.height() - paintUnit.descent())
            canvas.drawText(it, currentX, currentY, paintUnit)
        }

        canvas.drawPath(rightPath, paintIncDec)
    }



    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        if (event == null)
            return false

        val xCor = event.x
        val yCor = event.y

        when(event.action)
        {
            MotionEvent.ACTION_DOWN-> {
                onTouchDownEvent(xCor, yCor)
            }
            MotionEvent.ACTION_MOVE-> {
                onTouchMoveEvent(xCor, yCor)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL-> {
                log("isTouchInc= $isTouchInc    , isTouchDec= $isTouchDec")

                lastCor = 0f
                incDecSpeed = MIN_SPEED
                stopCounter()
            }
        }

        return true
    }

    private fun onTouchDownEvent(xCor: Float, yCor: Float) = when (orientation)
    {
        Orientation.Horizontal -> {
            when (xCor) {
                in 0..widthOfTriangle(orientation).roundToInt() /* Left Triangle */ -> {
                    lastCor = xCor

                    if (isLtR())
                        startDecreaseCounterInHorizontal()
                    else
                        startIncreaseCounterInHorizontal()
                }
                in (width - widthOfTriangle(orientation).roundToInt())..width /* Right Triangle */ -> {
                    lastCor = xCor

                    if (isLtR())
                        startIncreaseCounterInHorizontal()
                    else
                        startDecreaseCounterInHorizontal()
                }
                else -> {
                    isTouchInc = false
                    isTouchDec = false
                }
            }
        }
        Orientation.Vertical -> {
            when (yCor) {
                in 0..(heightOfTriangle(orientation).roundToInt()) -> {
                    isTouchInc = true
                    isTouchDec = false

                    lastCor = xCor
                    startIncreaseCounter()
                }
                in (height - heightOfTriangle(orientation)).roundToInt()..height -> {
                    isTouchDec = true
                    isTouchInc = false

                    lastCor = xCor
                    startDecreaseCounter()
                }
                else -> {
                    isTouchInc = false
                    isTouchDec = false
                }
            }
        }
    }

    private fun startDecreaseCounterInHorizontal()
    {
        isTouchDec = true
        isTouchInc = false

        startDecreaseCounter()
    }

    private fun startIncreaseCounterInHorizontal()
    {
        isTouchInc = true
        isTouchDec = false

        startIncreaseCounter()
    }

    private fun onTouchMoveEvent(xCor: Float, yCor: Float)
    {
        if (!isTouchInc && !isTouchDec)
            return

        //  (|- 5 +|  ,LtR)       ( |+ 5 -|  ,RTL)
        val isLTR = isLtR() && ( (isTouchInc && xCor > lastCor) || (isTouchDec && xCor < lastCor) )
        val isRTL = !isLtR() && ( ( isTouchInc && xCor < lastCor) || ( isTouchDec && xCor > lastCor) )

        if (orientation == Orientation.Horizontal && ( isLTR ||  isRTL))
        {
            val diff = abs(xCor - lastCor)
            val count = ceil(diff / corDiff).toInt()
            incDecSpeed = count

            log("count= $count      ,speed= $incDecSpeed ")
        }
        else if (orientation == Orientation.Vertical
                && ( (isTouchInc && yCor < lastCor) || (isTouchDec && yCor > lastCor) ) )
        {
            val diff = abs(yCor - lastCor)
            val count = ceil(diff / corDiff).toInt()
            incDecSpeed = count

            log("count= $count      ,speed= $incDecSpeed ")
        }
    }

    // 1 is Min speed
    private fun convertSpeedToDelay(value: Int) = when {
        value >= MAX_SPEED -> MIN_SPEED
        value <= MIN_SPEED -> MAX_SPEED
        else -> MAX_SPEED - value + 2
    }

    private val incRunnable by lazy { Runnable {
        if (maxNum != -1 && currentNum + stepNum > maxNum)
            return@Runnable

        currentNum += stepNum
        if (maxNum == -1)
            requestLayout()
        else
            invalidate()

        startIncreaseCounter(incDecSpeed.toLong())
    } }

    private val decRunnable by lazy { Runnable {
        if (minNum != -1 && currentNum - stepNum < minNum)
            return@Runnable

        currentNum -= stepNum
        if (maxNum == -1)
            requestLayout()
        else
            invalidate()

        startDecreaseCounter(incDecSpeed.toLong())
    } }

    private fun startIncreaseCounter(delay: Long = 0)
    {
        counterHandler.postDelayed(incRunnable, delay)
    }

    private fun startDecreaseCounter(delay: Long = 0)
    {
        counterHandler.postDelayed(decRunnable, delay)
    }

    private fun stopCounter()
    {
        counterHandler.removeCallbacks(incRunnable)
        counterHandler.removeCallbacks(decRunnable)
    }








    private fun heightOfTriangle(orientation: Orientation) = if (orientation == Orientation.Horizontal)
        widthOfTriangle()
    else
        heightOfTriangle()

    private fun widthOfTriangle(orientation: Orientation) = if (orientation == Orientation.Horizontal)
        heightOfTriangle()
    else
        widthOfTriangle()


    private fun heightOfTriangle() : Float = (sqrt(3f) * incDecLength) / 2

    private fun widthOfTriangle() : Float = incDecLength.toFloat()



    private fun isLtR() = (layoutDirection == LAYOUT_DIRECTION_LTR)

    private fun convertDPtoPX(dp: Int): Int {
        return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun convertSPToPX(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun log(data: String) = Log.d("saif", data)

    private fun log(data: String, e: Throwable) = Log.e("saif", data, e)




}

enum class Orientation(val value: Int) {
    Vertical(0), Horizontal(1)
}