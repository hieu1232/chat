package hieusenpaj.com.whapp.customImageView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable

class WaveView(context: Context, attrs: AttributeSet) : View(context, attrs) {


    private val mWavePaint = Paint()

    private val mWavePath = Path()
    private var mHalfWaveWidth = 0
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        innit()
    }

    private fun innit() {
        mHalfWaveWidth = measuredWidth / 4

        mWavePaint.color = Color.parseColor("#FFFFFF")
        mWavePaint.style = Paint.Style.FILL
        mWavePaint.isAntiAlias = true
        mWavePaint.isDither = true
//        mWavePaint.shader = LinearGradient(
//            0F,
//            0F,
//            measuredWidth.toFloat(),
//            measuredWidth.toFloat(),
//            -0xebebeb,
//            -0xebebeb,
//            Shader.TileMode.CLAMP
//        )

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(sizeWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(sizeHeight, MeasureSpec.EXACTLY)
        )
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mWavePath.reset()
        mWavePath.moveTo(0f, measuredHeight - measuredHeight / 4.toFloat())
        for (i in 0 until 7) {
            if (i % 2 == 0) {
                mWavePath.rQuadTo(
                    mHalfWaveWidth.toFloat(),
                    -measuredHeight / 8.toFloat(),
                    (mHalfWaveWidth * 2).toFloat(),
                    0f
                )
            } else {
                mWavePath.rQuadTo(
                    mHalfWaveWidth.toFloat(),
                    measuredHeight / 8.toFloat(),
                    (mHalfWaveWidth * 2).toFloat(),
                    0f
                )
            }
        }
        mWavePath.lineTo(measuredWidth.toFloat(), measuredHeight.toFloat())
        mWavePath.lineTo(-(measuredWidth / 2).toFloat(), measuredHeight.toFloat())
        mWavePath.close()
        canvas.drawPath(mWavePath, mWavePaint)
    }

}