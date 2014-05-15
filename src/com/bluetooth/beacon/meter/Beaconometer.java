package com.bluetooth.beacon.meter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.bluetooth.beacon.models.DistanceZone;

public final class Beaconometer extends View {

	private static final String TAG = Beaconometer.class.getSimpleName();
	private String mHotColdMeterValStrings[] = {"Frozen Solid", "Freezing", "Cold", "Hot","Burning Up", "ON FIRE"};
	public static final float mHotColdMeterHandleMovements[] = {372,311,240,182,110,47,47};
	public static final int mHotColdMeterBackgroundResources[] = {R.drawable.state_change_0,R.drawable.state_change_1,
															R.drawable.state_change_2,R.drawable.state_change_3,
															R.drawable.state_change_4,R.drawable.state_change_5, R.drawable.state_change_6};
	private int mHotColdMeterColors[]    = {Color.rgb(57,163,198),Color.rgb(57, 93, 198),Color.rgb(91, 57, 198),
													Color.rgb(247,91,104),Color.rgb(204, 51, 51),Color.rgb(153, 0, 0)}; 
	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Bitmap faceTexture;
	private Paint facePaint;
	private Paint rimShadowPaint;
	
	private Paint scalePaint;
	private RectF scaleRect;
	
	private Paint titlePaint;	
	private Path titlePath;

	private Paint logoPaint;
	private Bitmap logo;
	private Matrix logoMatrix;
	private float logoScale;
	
	private Paint handPaint;
	private Path handPath;
	private Paint handScrewPaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration
	private static final int totalNicks = 720;
	private static final float degreesPerNick = 720.0f / totalNicks;	
	private static final int centerDegree = 0; // the one in the top center (12 o'clock)
	private static final int minDegrees = 0;
	private static final int maxDegrees = 720;
	private static final int distanceBetweenDegrees = 59;
	
	
	// hand dynamics -- all are angular expressed in F degrees
	private boolean handInitialized = true;
	private float handPosition = centerDegree;
	private float handTarget = centerDegree;
	private float handVelocity = 0.0f;
	private float handAcceleration = 0.0f;
	private long lastHandMoveTime = -1L;
	private float mCurrentBeaconRangeValue = 0;
	private TransitionDrawable mBackground;
	private Integer transitionIndex;
    
	public Beaconometer(Context context) {
		super(context);
		init();
	}

	public Beaconometer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Beaconometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		handInitialized = bundle.getBoolean("handInitialized");
		handPosition = bundle.getFloat("handPosition");
		handTarget = bundle.getFloat("handTarget");
		handVelocity = bundle.getFloat("handVelocity");
		handAcceleration = bundle.getFloat("handAcceleration");
		lastHandMoveTime = bundle.getLong("lastHandMoveTime");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putBoolean("handInitialized", handInitialized);
		state.putFloat("handPosition", handPosition);
		state.putFloat("handTarget", handTarget);
		state.putFloat("handVelocity", handVelocity);
		state.putFloat("handAcceleration", handAcceleration);
		state.putLong("lastHandMoveTime", lastHandMoveTime);
		return state;
	}

	public void init() {
		initDrawingTools();
	}

	private String getTitle() {
		return "Hot Or Cold Meter";
	}

	private void initDrawingTools() {
		rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);

		// the linear gradient is a bit skewed for realism
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, 
										   Color.rgb(0xf0, 0xf5, 0xf0),
										   Color.rgb(0x30, 0x31, 0x30),
										   Shader.TileMode.CLAMP));		

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
		rimCirclePaint.setStrokeWidth(0.005f);

		float rimSize = 0.02f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);		

		faceTexture = BitmapFactory.decodeResource(getContext().getResources(), 
				   R.drawable.light_alum);
		BitmapShader paperShader = new BitmapShader(faceTexture, 
												    Shader.TileMode.MIRROR, 
												    Shader.TileMode.MIRROR);
		Matrix paperMatrix = new Matrix();
		facePaint = new Paint();
		facePaint.setFilterBitmap(true);
		paperMatrix.setScale(1.0f / faceTexture.getWidth(), 
							 1.0f / faceTexture.getHeight());
		paperShader.setLocalMatrix(paperMatrix);
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setShader(paperShader);

		rimShadowPaint = new Paint();
		rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect.width() / 2.0f, 
				   new int[] { 0x00000000, 0x00000500, 0x50000500 },
				   new float[] { 0.96f, 0.96f, 0.99f },
				   Shader.TileMode.MIRROR));
		rimShadowPaint.setStyle(Paint.Style.FILL);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(0x9f004d0f);
		scalePaint.setStrokeWidth(0.005f);
		scalePaint.setAntiAlias(true);
		
		scalePaint.setTextSize(0.045f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextScaleX(0.8f);
		scalePaint.setTextAlign(Paint.Align.CENTER);		
		
		float scalePosition = 0.10f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

		titlePaint = new Paint();
		titlePaint.setColor(0xaf946109);
		titlePaint.setAntiAlias(true);
		titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
		titlePaint.setTextAlign(Paint.Align.CENTER);
		titlePaint.setTextSize(0.05f);
		titlePaint.setTextScaleX(0.8f);

		titlePath = new Path();
		titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f, -180.0f);

		logoPaint = new Paint();
		logoPaint.setFilterBitmap(true);
		logo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher);
		logoMatrix = new Matrix();
		logoScale = (1.0f / logo.getWidth()) * 0.3f;;
		logoMatrix.setScale(logoScale, logoScale);

		handPaint = new Paint();
		handPaint.setAntiAlias(true);
		handPaint.setColor(0xff392f2c);		
		handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
		handPaint.setStyle(Paint.Style.FILL);	
		
		handPath = new Path();
		handPath.moveTo(0.5f, 0.5f + 0.2f);
		handPath.lineTo(0.5f - 0.010f, 0.5f + 0.2f - 0.007f);
		handPath.lineTo(0.5f - 0.002f, 0.5f - 0.32f);
		handPath.lineTo(0.5f + 0.002f, 0.5f - 0.32f);
		handPath.lineTo(0.5f + 0.010f, 0.5f + 0.2f - 0.007f);
		handPath.lineTo(0.5f, 0.5f + 0.2f);
		handPath.addCircle(0.5f, 0.5f, 0.025f, Path.Direction.CW);
		
		handScrewPaint = new Paint();
		handScrewPaint.setAntiAlias(true);
		handScrewPaint.setColor(0xff493f3c);
		handScrewPaint.setStyle(Paint.Style.FILL);
		
		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);

	}

	private void drawRim(Canvas canvas) {
		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
		// now the outer rim circle
		canvas.drawOval(rimRect, rimCirclePaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
		// draw the rim shadow inside the face
		canvas.drawOval(faceRect, rimShadowPaint);
	}

	private void drawScale(Canvas canvas) {
		canvas.drawOval(scaleRect, scalePaint);
		int k = 0;

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		for (int i = 1; i < totalNicks; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 - 0.020f;
						
			if (i % distanceBetweenDegrees == 0) {
				int value = nickToDegree(i);
				
				if (value >= minDegrees && value <= maxDegrees) {
					Path ntitlePath = new Path();
					ntitlePath.addArc(new RectF(0.14f, 0.14f, 0.76f, 0.76f), -50.0f, -50.0f);					
					String valueString = mHotColdMeterValStrings[k];
					titlePaint.setColor(mHotColdMeterColors[k]);
					canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);
					canvas.drawTextOnPath(valueString, ntitlePath, 0.0f,0.0f, titlePaint);					
					k++;					
				}
			}
			
			canvas.rotate(degreesPerNick, 0.5f, 0.5f);
		}
		canvas.restore();		
	}
	
	@SuppressLint("NewApi")
	private float getScaled(Canvas canvas, float in) {
	    return in * ( canvas.getDensity()==0 ? 1 : canvas.getDensity()/ 160.0f);
	}

	private int nickToDegree(int nick) {
		int rawDegree = ((nick < totalNicks / 2) ? nick : (nick - totalNicks)) * 2;
		int shiftedDegree = rawDegree + centerDegree;
		return shiftedDegree;
	}
	
	private float degreeToAngle(float degree) {
		return (degree - centerDegree) / 2.0f * degreesPerNick;
	}
	
	private void drawTitle(Canvas canvas) {
		String title = getTitle();
		titlePaint.setColor(Color.RED);
		canvas.drawTextOnPath(title, titlePath, 0.0f,0.0f, titlePaint);				
	}
	
	private void drawLogo(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(0.5f - logo.getWidth() * logoScale / 2.0f, 
						 0.5f - logo.getHeight() * logoScale / 2.0f);
		
		canvas.drawBitmap(logo, logoMatrix, logoPaint);
		canvas.restore();		
	}

	private void drawHand(Canvas canvas) {
		if (handInitialized) {
			float handAngle = degreeToAngle(handPosition);
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(handAngle, 0.5f, 0.5f);
			canvas.drawPath(handPath, handPaint);
			canvas.restore();
			
			canvas.drawCircle(0.5f, 0.5f, 0.01f, handScrewPaint);
		}
	}

	private void drawBackground(Canvas canvas) {
		if (background == null) {
			Log.w(TAG, "Background not created");
		} else {
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		drawBackground(canvas);

		float scale = (float) getWidth();		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);

		drawLogo(canvas);
		drawHand(canvas);
		
		canvas.restore();
	
		if (handNeedsToMove()) {
			moveHand();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "Size changed to " + w + "x" + h);
		
		regenerateBackground();
	}
	
	private void regenerateBackground() {
		// free the old bitmap
		if (background != null) {
			background.recycle();
		}
				
		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();		
		backgroundCanvas.scale(scale, scale);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);
		drawTitle(backgroundCanvas);		
			
	}
	
	private boolean handNeedsToMove() {
		return Math.abs(handPosition - handTarget) > 0.01f;
	}
	
	private void moveHand() {
		if (! handNeedsToMove()) {
			return;
		}
		
		if (lastHandMoveTime != -1L) {
			long currentTime = System.currentTimeMillis();
			float delta = (currentTime - lastHandMoveTime) / 1000.0f;

			float direction = Math.signum(handVelocity);
			if (Math.abs(handVelocity) < 90.0f) {
				handAcceleration = 5.0f * (handTarget - handPosition);
			} else {
				handAcceleration = 0.0f;
			}
			handPosition += handVelocity * delta;
			handVelocity += handAcceleration * delta;
			if ((handTarget - handPosition) * direction < 0.01f * direction) {
				handPosition = handTarget;
				handVelocity = 0.0f;
				handAcceleration = 0.0f;
				lastHandMoveTime = -1L;
			} else {
				lastHandMoveTime = System.currentTimeMillis();				
			}
			invalidate();
		} else {
			lastHandMoveTime = System.currentTimeMillis();
			moveHand();
		}
	}
	
	
	public void onBeaconNewReading(float bRange) {
		float temperatureF = (9.0f / 5.0f) * bRange + 32.0f;
		setHandTarget(temperatureF);		
	}

	private void setHandTarget(float nBeaconRange) {
		if (nBeaconRange < minDegrees) {
			nBeaconRange = minDegrees;
		} else if (nBeaconRange > maxDegrees) {
			nBeaconRange = maxDegrees;
		}
		handTarget = nBeaconRange;
		handInitialized = true;
		invalidate();
	}
	
	public void changeTransition(DistanceZone zone) {
		
		//if we are still at the same zone, don't do anything
		if (mCurrentBeaconRangeValue == mHotColdMeterHandleMovements[zone.ordinal()]) {
			return;
		}
		mCurrentBeaconRangeValue = mHotColdMeterHandleMovements[zone.ordinal()];
        Log.d(TAG, "R CHANGE VAL:" + zone.ordinal() + " " + zone.name());
        Log.d(TAG, "mCurrentBeaconRangeValue:" + mCurrentBeaconRangeValue);
		onBeaconNewReading(mCurrentBeaconRangeValue);
		if (transitionIndex == null) {
			transitionIndex = zone.ordinal();
		}
		if (transitionIndex < zone.ordinal()) {
			this.setBackgroundResource( mHotColdMeterBackgroundResources[zone.ordinal()]);
			mBackground = (TransitionDrawable) this.getBackground();
			mBackground.setCrossFadeEnabled(true);
			// You can pass the duration of the animation in milliseconds here
			mBackground.startTransition(3000);
				
		} else {
			this.setBackgroundResource( mHotColdMeterBackgroundResources[zone.ordinal()]);
			mBackground = (TransitionDrawable) this.getBackground();
			mBackground.setCrossFadeEnabled(true);
			mBackground.startTransition(3000);
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler =  new Handler() {};
	
}
