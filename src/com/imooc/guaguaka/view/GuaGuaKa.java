package com.imooc.guaguaka.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.imooc.guaguaka.R;

public class GuaGuaKa extends View {

	private Paint mOutterPaint;
	private Path mPath;
	private Canvas mCanvas;
	private Bitmap mBitmap;
	
	private int mLastX;
	private int mLastY;
	
	private Bitmap mOutterBitmap;
	//-------------------------
	//private Bitmap bitmap;//step2
	private String mText;
	private Paint mBackPaint;
	/**
	 * 记录刮奖信息的宽和高
	 */
	private Rect mTextBound;
	private int mTextSize;
	private int mTextColor;
	//判断遮盖区域是否消除
	private volatile boolean mComplete=false;
	/**
	 * 刮完回调
	 * @author wxy
	 *
	 */
	public interface OnGuaGuaKaCompleteListener{
		void complete();
	}
	private OnGuaGuaKaCompleteListener mListener;
	
	public void setOnGuaGuaKaCompleteListener(OnGuaGuaKaCompleteListener mListener) {
		this.mListener = mListener;
	}

	public void setText(String mText) {
		this.mText = mText;
		//获得当前画笔绘制文本的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	public GuaGuaKa(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		TypedArray a = null;
		try{
			a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaKa, defStyle, 0);
			int n = a.getIndexCount();
			for (int i = 0; i < n; i++) {
				int attr = a.getIndex(i);
				switch(attr){
				case R.styleable.GuaGuaKa_text:
					mText = a.getString(attr);
					break;
				case R.styleable.GuaGuaKa_textSize:
					mTextSize = (int)a.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));
					break;
				case R.styleable.GuaGuaKa_textColor:
					mTextColor=a.getColor(attr, 0x000000);
					break;
				}
				
			}
		}finally{
			if(a != null){
				a.recycle();
			}
		}
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = getMeasuredWidth();
		int height= getMeasuredHeight();
		//初始化画板
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		
		//设置绘制path画笔的一些属性
		setupOutPaint();
		setUpBackPaint();
		//mCanvas.drawColor(Color.parseColor("#c0c0c0"));//step2
		mCanvas.drawRoundRect(new RectF(0,0,width,height), 30, 30, mOutterPaint);
		mCanvas.drawBitmap(mOutterBitmap, null,new Rect(0,0,width,height), null);
	}
	/**
	 * 设置绘制获奖信息的画笔属性
	 */
	private void setUpBackPaint() {
		mBackPaint.setColor(mTextColor);//Color.DKGRAY);
		mBackPaint.setStyle(Style.FILL);
		mBackPaint.setTextSize(mTextSize);
		//获得当前画笔绘制文本的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX=x;
			mLastY=y;
			mPath.moveTo(mLastX, mLastY);
			break;
		case MotionEvent.ACTION_MOVE:
			int dx=Math.abs(x-mLastX);
			int dy=Math.abs(y-mLastY);
			
			if(dx > 3 || dy >3){
				mPath.lineTo(x, y);
			}
			
			mLastX=x;
			mLastY=y;
			break;
		case MotionEvent.ACTION_UP:
			new Thread(mRunnable).start();
			break;
		default:
			break;
		}
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawBitmap(bitmap, 0,0, null);//step2
		canvas.drawText(mText, getWidth()/2-mTextBound.width()/2, getHeight()/2+mTextBound.height()/2, mBackPaint);
		if(mComplete){
			if(mListener != null){
				mListener.complete();
			}
		}
		if(!mComplete){
			drawPath();
			canvas.drawBitmap(mBitmap, 0, 0,null);
		}
	}
	private void drawPath()
	{
		mOutterPaint.setStyle(Style.STROKE);
		mOutterPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));//step2
		mCanvas.drawPath(mPath, mOutterPaint);
	}
	/**
	 * 设置绘制path画笔的一些属性
	 */
	private void setupOutPaint(){
		mOutterPaint.setColor(Color.parseColor("#c0c0c0"));//.RED);
		mOutterPaint.setAntiAlias(true);
		mOutterPaint.setDither(true);
		mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
		mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
		mOutterPaint.setStyle(Style.FILL);//.STROKE);
		mOutterPaint.setStrokeWidth(20);
	}
	/**
	 * 进行初始化操作
	 */
	private void init() {
		mOutterPaint=new Paint();
		mPath=new Path();
		
		//bitmap=BitmapFactory.decodeResource(getResources(),com.imooc.guaguaka.R.drawable.t2);//step2
		mOutterBitmap = BitmapFactory.decodeResource(getResources(), com.imooc.guaguaka.R.drawable.fg_guaguaka);
		mText="谢谢惠顾";
		mTextBound=new Rect();
		mBackPaint=new Paint();
		mTextSize=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());//30;
	}
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			int w=getWidth();
			int h=getHeight();
			
			float wipeArea=0;
			float totalArea=w*h;
			Bitmap bitmap=mBitmap;
			int[] mPixels=new int[w*h];
			
			//获得bitmap上所有的像素信息
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
			Log.e("TAG", mPixels.length+"");
			for(int i=0;i<w;i++){
				for(int j=0;j<h;j++){
					int index = i+j*w;
					if(mPixels[index]==0){
						wipeArea++;
					}
				}
			}
			if(wipeArea > 0 && totalArea > 0){
				int percent = (int)(wipeArea*100/totalArea);
				Log.e("TAG", percent+"");
				if(percent>60){
					//清除掉图层区域
					mComplete=true;
					postInvalidate();
				
				}
			}
		}
	};
	public GuaGuaKa(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		//context.obtainStyledAttributes(set, attrs)
	}

	public GuaGuaKa(Context context) {
		this(context,null);
	}

}
