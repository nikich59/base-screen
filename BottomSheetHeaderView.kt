package uz.uzum.tezkor.courier.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.google.android.material.math.MathUtils.lerp
import uz.uzum.tezkor.courier.base_screen.R
import uz.uzum.tezkor.uikit.drawable.ConvexTopDrawable
import uz.uzum.tezkor.uikit.util.dpToPx
import kotlin.math.roundToInt

class BottomSheetHeaderView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val expandedTitle = AppCompatTextView(context).apply {
        setTextAppearance(R.style.TextAppearance_Uzum_PageTitle)
        setTextColor(context.getColor(R.color.neutral_950))

        updatePadding(
            left = context.dpToPx(16f).roundToInt(),
            top = context.dpToPx(32f).roundToInt(),
            right = context.dpToPx(16f).roundToInt(),
            bottom = context.dpToPx(16f).roundToInt(),
        )
        id = generateViewId()
    }
    private val collapsedTitle = AppCompatTextView(context).apply {
        setTextAppearance(R.style.TextAppearance_Uzum_NavigationBarTitle)
        setTextColor(context.getColor(R.color.neutral_950))
        gravity = Gravity.CENTER

        updatePadding(
            left = context.dpToPx(68f).roundToInt(),
            top = context.dpToPx(20f).roundToInt(),
            right = context.dpToPx(68f).roundToInt(),
            bottom = context.dpToPx(20f).roundToInt(),
        )
        id = generateViewId()
    }

    private val closeButton = AppCompatImageView(context).apply {
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bottom_sheet_header_close_icon))
        background = ContextCompat.getDrawable(context, R.drawable.oval_black)
        backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.secondary_50))

        foreground = RippleDrawable(
            ColorStateList.valueOf(context.getColor(R.color.press_ripple)),
            null,
            background,
        )

        id = generateViewId()

        setOnClickListener {
            onCloseClick()
        }
    }

    private val bottomSeparator = View(context).apply {
        setBackgroundColor(context.getColor(R.color.neutral_100))
    }

    private var onCloseClick: () -> Unit = { }

    private val convexTopDrawable = ConvexTopDrawable(
        context.getColor(R.color.neutral_00),
        context.resources.getDimension(R.dimen.convex_top_drawable_bump_height),
    )
    private var expandedProgress: Float = 1f

    init {
        addView(
            closeButton,
            LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.WRAP_CONTENT).apply {
                topToTop = LayoutParams.PARENT_ID
                endToEnd = LayoutParams.PARENT_ID

                topMargin = context.dpToPx(16f).roundToInt()
                marginEnd = context.dpToPx(16f).roundToInt()
            },
        )

        addView(
            expandedTitle,
            LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.WRAP_CONTENT).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
                endToStart = closeButton.id
            },
        )

        addView(
            collapsedTitle,
            LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.WRAP_CONTENT).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
                endToEnd = LayoutParams.PARENT_ID
            },
        )

        addView(
            bottomSeparator,
            LayoutParams(LayoutParams.MATCH_CONSTRAINT, context.dpToPx(1f).roundToInt()).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
                endToEnd = LayoutParams.PARENT_ID
            }
        )

        setExpandedProgress(1f)
    }

    fun updateBottomOffset(offset: Int) {
        val offsetAmplitude = getExpandedHeight() - getCollapsedHeight()
        val progress = if (offsetAmplitude > 0f) {
            ((offsetAmplitude - offset) / offsetAmplitude).coerceIn(0f, 1f)
        } else {
            1f
        }

        setExpandedProgress(progress)
    }

    fun setCollapsed() {
        setExpandedProgress(0f)
    }

    fun setTitle(title: String) {
        expandedTitle.text = title
        collapsedTitle.text = title
    }

    fun setOnCloseClick(action: () -> Unit) {
        this.onCloseClick = action
    }

    fun getExpandedProgress(): Float {
        return expandedProgress
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.y <= getContentBottom()) {
                super.dispatchTouchEvent(event)
            } else {
                false
            }
        } else {
            super.dispatchTouchEvent(event)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        convexTopDrawable.setBounds(
            0,
            0,
            width,
            getContentBottom(),
        )
        convexTopDrawable.draw(canvas)

        super.dispatchDraw(canvas)
    }

    private fun setExpandedProgress(expandedProgress: Float) {
        this.expandedProgress = expandedProgress

        expandedTitle.alpha = lerp(0f, 1f, expandedProgress)
        expandedTitle.translationY = lerp(
            -(getExpandedHeight() - getCollapsedHeight()),
            0f,
            expandedProgress,
        )

        collapsedTitle.alpha = lerp(1f, 0f, expandedProgress)
        collapsedTitle.translationY = lerp(
            0f,
            getExpandedHeight() - getCollapsedHeight(),
            expandedProgress,
        )

        val closeButtonScale = lerp(0.85f, 1f, expandedProgress)
        closeButton.scaleX = closeButtonScale
        closeButton.scaleY = closeButtonScale

        bottomSeparator.translationY = getContentBottom().toFloat() - bottomSeparator.measuredHeight
        bottomSeparator.alpha = lerp(1f, 0f, expandedProgress)

        invalidate()
    }

    fun getExpandedHeight(): Float {
        return expandedTitle.measuredHeight.toFloat()
    }

    private fun getCollapsedHeight(): Float {
        return collapsedTitle.measuredHeight.toFloat()
    }

    private fun getContentBottom(): Int {
        return lerp(getCollapsedHeight(), getExpandedHeight(), expandedProgress).roundToInt()
    }
}
