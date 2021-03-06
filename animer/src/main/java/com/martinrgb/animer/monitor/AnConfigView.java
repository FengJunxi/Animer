package com.martinrgb.animer.monitor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.martinrgb.animer.Animer;
import com.martinrgb.animer.R;
import com.martinrgb.animer.core.interpolator.AnInterpolator;
import com.martinrgb.animer.core.math.converter.DHOConverter;
import com.martinrgb.animer.core.math.converter.OrigamiPOPConverter;
import com.martinrgb.animer.core.math.converter.RK4Converter;
import com.martinrgb.animer.core.math.converter.UIViewSpringConverter;
import com.martinrgb.animer.monitor.fps.FPSDetector;
import com.martinrgb.animer.monitor.fps.FrameDataCallback;
import com.martinrgb.animer.monitor.shader.ShaderSurfaceView;

import java.text.DecimalFormat;

public class AnConfigView extends FrameLayout {

    private Spinner mSolverObjectSelectorSpinner,mSolverTypeSelectorSpinner;
    private AnSpinnerAdapter solverObjectSpinnerAdapter,solverTypeSpinnerAdapter;
    private Animer currentAnimer,mRevealAnimer,mFPSAnimer;
    private AnConfigRegistry anConfigRegistry;
    private LinearLayout listLayout;
    private SeekbarListener seekbarListener;
    private SolverSelectedListener solverSelectedListener;
    private ShaderSurfaceView shaderSurfaceView;
    //private final int mTextColor = Color.argb(255, 255, 255, 255);

    private int mainColor;
    private int secondaryColor;
    private int backgroundColor;
    private int fontSize;
    private Typeface typeface;

    private String currentObjectType = "NULL";

    private int listSize = 2;
    private static int SEEKBAR_START_ID = 15000;
    private static int SEEKLABEL_START_ID_START_ID = 20000;
    private static int EDITTEXT_START_ID_START_ID = 25000;
    private static final int MAX_SEEKBAR_VAL = 100000;
    private static final int MIN_SEEKBAR_VAL = 1;
    //TODO
    private static final DecimalFormat DECIMAL_FORMAT_2 = new DecimalFormat("#.##");
    private static final DecimalFormat DECIMAL_FORMAT_1 = new DecimalFormat("#.#");
    private static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("#.###");
    private float MAX_VAL1,MAX_VAL2,MAX_VAL3,MAX_VAL4,MAX_VAL5;
    private float[] MAX_VALUES = new float[]{MAX_VAL1,MAX_VAL2,MAX_VAL3,MAX_VAL4,MAX_VAL5};
    private float MIN_VAL1,MIN_VAL2,MIN_VAL3,MIN_VAL4,MIN_VAL5;
    private float[] MIN_VALUES = new float[]{MIN_VAL1,MIN_VAL2,MIN_VAL3,MIN_VAL4,MIN_VAL5};
    private float RANGE_VAL1,RANGE_VAL2,RANGE_VAL3,RANGE_VAL4,RANGE_VAL5;
    private float[] RANGE_VALUES = new float[]{RANGE_VAL1,RANGE_VAL2,RANGE_VAL3,RANGE_VAL4,RANGE_VAL5};
    private float seekBarValue1,seekBarValue2,seekBarValue3,seekBarValue4,seekBarValue5;
    private Object[] SEEKBAR_VALUES = new Object[]{seekBarValue1,seekBarValue2,seekBarValue3,seekBarValue4,seekBarValue5};
    private TextView mArgument1SeekLabel,mArgument2SeekLabel,mArgument3SeekLabel,mArgument4SeekLabel,mArgument5SeekLabel;
    private TextView[] SEEKBAR_LABElS = new TextView[]{mArgument1SeekLabel,mArgument2SeekLabel,mArgument3SeekLabel,mArgument4SeekLabel,mArgument5SeekLabel};
    private EditText mArgument1EditText,mArgument2EditText,mArgument3EditText,mArgument4EditText,mArgument5EditText;
    private EditText[] EDITTEXTS = new EditText[]{mArgument1EditText,mArgument2EditText,mArgument3EditText,mArgument4EditText,mArgument5EditText};
    private SeekBar mArgument1SeekBar,mArgument2SeekBar,mArgument3SeekBar,mArgument4SeekBar,mArgument5SeekBar;
    private SeekBar[] SEEKBARS = new SeekBar[]{mArgument1SeekBar,mArgument2SeekBar,mArgument3SeekBar,mArgument4SeekBar,mArgument5SeekBar};

    private final int MARGIN_SIZE = (int) getResources().getDimension(R.dimen.margin_size);
    private final int PADDING_SIZE = (int) getResources().getDimension(R.dimen.padding_size);
    private final int PX_120 = dpToPx(120, getResources());
    private TextView fpsView;

    private AnConfigMap<String,Animer.AnimerSolver> mSolverTypesMap;
    private AnConfigMap<String,Animer> mAnimerObjectsMap;

    private Animer.TriggeredListener triggeredListener;

    private Context mContext;

    public AnConfigView(Context context) {
        this(context, null);
    }

    public AnConfigView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnConfigView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private boolean hadInited = false;
    private void initView(Context context) {
        typeface = Typeface.createFromAsset(context.getAssets(), "Montserrat-SemiBold.ttf");
        secondaryColor = ContextCompat.getColor(context, R.color.secondaryColor);
        mainColor = ContextCompat.getColor(context,R.color.mainColor);
        backgroundColor = ContextCompat.getColor(context,R.color.backgroundColor);
        fontSize = getResources().getDimensionPixelSize(R.dimen.font_size);
        View view = inflate(getContext(), R.layout.config_view, null);
        addView(view);


        //Log.e("r: ",String.valueOf() + "g:" + String.valueOf(Color.green(mainColor)) + "b:" + String.valueOf(String.valueOf(Color.blue(mainColor))) );

        fpsView = findViewById(R.id.fps_view);
        fpsView.setTypeface(typeface);
        fpsView.setTextSize(fontSize);
        fpsView.setOnTouchListener(new OnFPSTouchListener());

        shaderSurfaceView = findViewById(R.id.shader_surfaceview);
        shaderSurfaceView.setFactorInput(1500,0);
        shaderSurfaceView.setFactorInput(0.5f,1);
        shaderSurfaceView.setMainColor((float)Color.red(mainColor)/255.f,(float) Color.green(mainColor)/255.f,(float) Color.blue(mainColor)/255.f  );
        Log.e("rgb-r:", String.valueOf((float)Color.red(mainColor)/255.f));
        Log.e("rgb-g:", String.valueOf((float)Color.green(mainColor)/255.f));
        Log.e("rgb-b:", String.valueOf((float)Color.blue(mainColor)/255.f));

        shaderSurfaceView.setSecondaryColor((float)Color.red(secondaryColor)/255.f,(float) Color.green(secondaryColor)/255.f,(float) Color.blue(secondaryColor)/255.f );
        mContext = context;

        // ## Spinner
        anConfigRegistry = AnConfigRegistry.getInstance();
        triggeredListener = new Animer.TriggeredListener() {
            @Override
            public void onTrigger(boolean triggered) {

                shaderSurfaceView.resetTime();

                //TODO ResetWhen Request
                if(triggered){
                    shaderSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                }
                else{
                    shaderSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                }

            }
        };

        solverObjectSpinnerAdapter = new AnSpinnerAdapter(context,getResources());
        solverTypeSpinnerAdapter = new AnSpinnerAdapter(context,getResources());

        mSolverObjectSelectorSpinner = findViewById(R.id.object_spinner);
        mSolverTypeSelectorSpinner = findViewById(R.id.type_spinner);

        solverSelectedListener = new SolverSelectedListener();
        seekbarListener = new SeekbarListener();

        mSolverObjectSelectorSpinner.setAdapter(solverObjectSpinnerAdapter);
        mSolverObjectSelectorSpinner.setOnItemSelectedListener(solverSelectedListener);

        mSolverTypeSelectorSpinner.setAdapter(solverTypeSpinnerAdapter);
        mSolverTypeSelectorSpinner.setOnItemSelectedListener(solverSelectedListener);

        refreshAnimerConfigs();

        // ## List
        listLayout = findViewById(R.id.list_layout);

        // ## Nub
        View nub = findViewById(R.id.nub);
        nub.setOnTouchListener(new OnNubTouchListener());

        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Put your code here.
                if(!hadInited) {
                    mRevealAnimer = new Animer();
                    mRevealAnimer.setSolver(Animer.springDroid(500, 0.95f));
                    mRevealAnimer.setUpdateListener(new Animer.UpdateListener() {
                        @Override
                        public void onUpdate(float value, float velocity, float progress) {
                            AnConfigView.this.setTranslationY(value);
                        }
                    });

                    mFPSAnimer = new Animer();
                    mFPSAnimer.setSolver(Animer.springDroid(600, 0.7f));
                    mFPSAnimer.setUpdateListener(new Animer.UpdateListener() {
                        @Override
                        public void onUpdate(float value, float velocity, float progress) {
                            fpsView.setScaleX(value);
                            fpsView.setScaleY(value);
                        }
                    });
                    mRevealAnimer.setCurrentValue(-(AnConfigView.this.getMeasuredHeight() - getResources().getDimension(R.dimen.nub_height)));
                    mFPSAnimer.setCurrentValue(1);
                    hadInited = true;


                    float maxValue = 0;
                    float minValue = -(AnConfigView.this.getMeasuredHeight() - getResources().getDimension(R.dimen.nub_height));
                    if(setRevealed){
                        mRevealAnimer.setCurrentValue(maxValue);
                        nub.setVisibility(INVISIBLE);
                    }
                    else{
                        mRevealAnimer.setCurrentValue(minValue);
                        nub.setVisibility(VISIBLE);
                    }
                }
            }
        });

        this.setElevation(1000);
    }


    public void refreshAnimerConfigs() {
        mAnimerObjectsMap = anConfigRegistry.getAllAnimer();
        solverObjectSpinnerAdapter.clear();

        for(int i = 0; i< mAnimerObjectsMap.size(); i++){
            solverObjectSpinnerAdapter.add(String.valueOf(mAnimerObjectsMap.getKey(i)));
        }
        solverObjectSpinnerAdapter.notifyDataSetChanged();

        if (solverObjectSpinnerAdapter.getCount() > 0) {
            // object first time selection
            mSolverObjectSelectorSpinner.setSelection(0);
            initTypeConfigs();
        }



        mSolverTypesMap = anConfigRegistry.getAllSolverTypes();
        solverTypeSpinnerAdapter.clear();
        for(int i = 0; i< mSolverTypesMap.size(); i++){
            solverTypeSpinnerAdapter.add(String.valueOf(mSolverTypesMap.getKey(i)));
        }
        solverTypeSpinnerAdapter.notifyDataSetChanged();

        if (solverObjectSpinnerAdapter.getCount() > 0) {
            // solver first time selection
            if(currentAnimer !=null && currentAnimer.getTriggerListener() !=null){
                currentAnimer.removeTriggerListener();
            }
            currentAnimer = (Animer) mAnimerObjectsMap.getValue(0);
            currentAnimer.setTriggerListener(triggeredListener);
            //shaderSurfaceView.requestRender();

            recreateList();
            int typeIndex = 0;
            // select the right interpolator
            if(String.valueOf(currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("converter_type")) == "AndroidInterpolator"){
                typeIndex = mSolverTypesMap.getIndexByString(String.valueOf(currentAnimer.getCurrentSolver().getArg1().getClass().getSimpleName()));
            }
            // select the right animator
            else{
                typeIndex = mSolverTypesMap.getIndexByString(String.valueOf(currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("converter_type")));
            }
            mSolverTypeSelectorSpinner.setSelection(typeIndex,false);
        }
    }

    private void initTypeConfigs() {
        mSolverTypesMap = anConfigRegistry.getAllSolverTypes();
        solverTypeSpinnerAdapter.clear();

        for(int i = 0; i< mSolverTypesMap.size(); i++){
            solverTypeSpinnerAdapter.add(String.valueOf(mSolverTypesMap.getKey(i)));
        }

        solverTypeSpinnerAdapter.notifyDataSetChanged();
        if (solverObjectSpinnerAdapter.getCount() > 0) {
            // solver first time selection
            if(currentAnimer !=null && currentAnimer.getTriggerListener() !=null){
                currentAnimer.removeTriggerListener();
            }
            currentAnimer = (Animer) mAnimerObjectsMap.getValue(0);
            currentAnimer.setTriggerListener(triggeredListener);
            //shaderSurfaceView.requestRender();

            recreateList();
            int typeIndex = 0;
            // select the right interpolator
            if(String.valueOf(currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("converter_type")) == "AndroidInterpolator"){
                typeIndex = mSolverTypesMap.getIndexByString(String.valueOf(currentAnimer.getCurrentSolver().getArg1().getClass().getSimpleName()));
            }
            // select the right animator
            else{
                typeIndex = mSolverTypesMap.getIndexByString(String.valueOf(currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("converter_type")));
            }
            mSolverTypeSelectorSpinner.setSelection(typeIndex,false);
        }
    }

    private void recreateList(){
        FrameLayout.LayoutParams params;
        LinearLayout seekWrapper;
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        tableLayoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);


        listLayout.removeAllViews();
        if(currentAnimer.getCurrentSolverData().getKeyByString("converter_type").toString() != "AndroidInterpolator") {
            listSize =2;
        }
        else{
            AnInterpolator mInterpolator = (AnInterpolator) currentAnimer.getCurrentSolver().getArg1();
            listSize = 1 + (mInterpolator.getArgNum()) ;
        }
        for (int i = 0;i<listSize;i++){
            seekWrapper = new LinearLayout(mContext);
            params = createLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);
            seekWrapper.setPadding(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);
            seekWrapper.setLayoutParams(params);
            seekWrapper.setOrientation(LinearLayout.HORIZONTAL);
            //seekWrapper.setBackgroundColor(Color.BLACK);
            listLayout.addView(seekWrapper);

            SEEKBAR_LABElS[i] = new TextView(getContext());
            params = createLayoutParams(dpToPx(108,getResources()), ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);
            SEEKBAR_LABElS[i].setLayoutParams(params);
            SEEKBAR_LABElS[i].setPadding(PADDING_SIZE + dpToPx(8,getResources()), PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);
            SEEKBAR_LABElS[i].setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            SEEKBAR_LABElS[i].setTextColor(secondaryColor);
            SEEKBAR_LABElS[i].setTextSize(fontSize-1);
            SEEKBAR_LABElS[i].setMaxLines(1);
            SEEKBAR_LABElS[i].setTypeface(typeface);
            SEEKBAR_LABElS[i].setId(SEEKLABEL_START_ID_START_ID + i);
            SEEKBAR_LABElS[i].setAlpha(0.6f);
            seekWrapper.addView(SEEKBAR_LABElS[i]);

            EDITTEXTS[i] = new EditText(getContext());
            params = createLayoutParams(dpToPx(70,getResources()),ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(MARGIN_SIZE,MARGIN_SIZE,MARGIN_SIZE,MARGIN_SIZE);
            EDITTEXTS[i].setLayoutParams(params);
            EDITTEXTS[i].setPadding(PADDING_SIZE,PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);
            EDITTEXTS[i].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            EDITTEXTS[i].setTextColor(secondaryColor);
            EDITTEXTS[i].setTextAlignment(TEXT_ALIGNMENT_CENTER);
            EDITTEXTS[i].setHint("0");
            EDITTEXTS[i].setHintTextColor(secondaryColor);
            EDITTEXTS[i].setBackground(ContextCompat.getDrawable(mContext,R.drawable.ic_edit_border));
            EDITTEXTS[i].setGravity(Gravity.LEFT);
            EDITTEXTS[i].setTypeface(typeface);
            SEEKBAR_LABElS[i].setId(EDITTEXT_START_ID_START_ID + i);
            EDITTEXTS[i].addTextChangedListener(new EditTextListener(EDITTEXTS[i],i));

            //TODO Refelection for old version
//            EDITTEXTS[i].setTextCursorDrawable(ContextCompat.getDrawable(mContext,R.drawable.text_cursor));

            //EDITTEXTS[i].setTextCursorDrawable(ContextCompat.getDrawable(mContext,R.drawable.text_cursor));
            EDITTEXTS[i].setTextSize(fontSize);
            seekWrapper.addView(EDITTEXTS[i]);

            SEEKBARS[i] =  new SeekBar(mContext);
            params = createLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);
            params.gravity = Gravity.CENTER_VERTICAL;
            SEEKBARS[i].setLayoutParams(params);
            SEEKBARS[i].setPadding(PADDING_SIZE+ dpToPx(4,getResources()), PADDING_SIZE+ (fontSize - 10)*2, PADDING_SIZE + dpToPx(16,getResources()), PADDING_SIZE+ (fontSize - 10)*2);
            SEEKBARS[i].setId(SEEKBAR_START_ID + i);
            SEEKBARS[i].setProgressBackgroundTintList(ColorStateList.valueOf(secondaryColor));
            SEEKBARS[i].setProgressTintList(ColorStateList.valueOf(mainColor));
            SEEKBARS[i].setThumb(ContextCompat.getDrawable(mContext,R.drawable.ic_thumb));
            //SEEKBARS[i].setBackgroundColor(Color.RED);
            seekWrapper.addView(SEEKBARS[i]);

            SEEKBARS[i].setMax(MAX_SEEKBAR_VAL);
            SEEKBARS[i].setMin(MIN_SEEKBAR_VAL);
            SEEKBARS[i].setOnSeekBarChangeListener(seekbarListener);

        }
    }


    private int typeChecker,objectChecker = 0;
    private boolean typeSpinnerIsFixedSelection = false;
    private int prevTypeIndex = -1,typeIndex = -1;

    private class SolverSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


            if(adapterView == mSolverObjectSelectorSpinner){
                // get animer from Map
                solverObjectSpinnerAdapter.setSelectedItemIndex(i);
                if(currentAnimer !=null && currentAnimer.getTriggerListener() !=null){
                    currentAnimer.removeTriggerListener();
                }
                if(typeIndex !=-1){
                    prevTypeIndex = typeIndex;
                }
                currentAnimer = (Animer) mAnimerObjectsMap.getValue(i);
                currentAnimer.setTriggerListener(triggeredListener);
                //shaderSurfaceView.requestRender();

                recreateList();
                redefineMinMax(currentAnimer.getCurrentSolver());
                updateSeekBars(currentAnimer.getCurrentSolver());

                // will not excute in init
                if(objectChecker > 0){
                    typeSpinnerIsFixedSelection = true;
                    // select the right interpolator
                    if(String.valueOf(currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("converter_type")).toString().contains("AndroidInterpolator")){
                        typeIndex = mSolverTypesMap.getIndexByString(String.valueOf(currentAnimer.getCurrentSolver().getArg1().getClass().getSimpleName()));
                    }
                    // select the right animator
                    else{
                        typeIndex = mSolverTypesMap.getIndexByString(currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("converter_type").toString());
                    }

                    // when aniamtor type is equal,fix bugs
                    if(mSolverTypeSelectorSpinner.getSelectedItemPosition() == typeIndex){
                        typeSpinnerIsFixedSelection = false;
                    }

                    mSolverTypeSelectorSpinner.setSelection(typeIndex,false);
                }

                objectChecker++;
            }
            else if (adapterView == mSolverTypeSelectorSpinner){
                // will not excute in init
                solverTypeSpinnerAdapter.setSelectedItemIndex(i);
                if(typeChecker > 0) {
                    if(typeSpinnerIsFixedSelection){
                        typeSpinnerIsFixedSelection = false;
                    }
                    //TODO Remeber Parameters Before
                    else{
                        // reset animer from Map
                        Animer.AnimerSolver seltectedSolver = (Animer.AnimerSolver) mSolverTypesMap.getValue(i);
                        currentAnimer.setSolver(seltectedSolver);
                        recreateList();
                        redefineMinMax(currentAnimer.getCurrentSolver());
                        updateSeekBars(currentAnimer.getCurrentSolver());
                    }
                }
                typeChecker++;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private void redefineMinMax(Animer.AnimerSolver animerSolver){
        currentObjectType = animerSolver.getConfigSet().getKeyByString("converter_type").toString();

        if(currentObjectType != "AndroidInterpolator"){
            for (int index = 0;index<listSize;index++){
                    MAX_VALUES[index] = Float.valueOf(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(index+1) +"_max").toString());
                    MIN_VALUES[index] = Float.valueOf(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(index+1) +"_min").toString());
                    RANGE_VALUES[index] = MAX_VALUES[index] - MIN_VALUES[index];
            }
        }
        else{
            for (int index = 0;index<listSize-1;index++){

                MAX_VALUES[index] = ((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).getArgMax(index);;
                MIN_VALUES[index] =  ((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).getArgMin(index);;
                RANGE_VALUES[index] = MAX_VALUES[index] - MIN_VALUES[index];
            }

            MAX_VALUES[listSize-1] = Float.valueOf(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(2) +"_max").toString());
            MIN_VALUES[listSize-1] = Float.valueOf(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(2) +"_min").toString());
            RANGE_VALUES[listSize -1] = MAX_VALUES[listSize -1] - MIN_VALUES[listSize - 1];
        }
    }

    private void updateSeekBars(Animer.AnimerSolver animerSolver) {

        if(currentObjectType != "AndroidInterpolator") {
            for(int i = 0;i<listSize;i++){
                    SEEKBAR_VALUES[i] = Float.valueOf(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(i + 1)).toString());
                    float progress = ((float) SEEKBAR_VALUES[i] - MIN_VALUES[i]) / RANGE_VALUES[i] * (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL) + MIN_SEEKBAR_VAL;
                    SEEKBARS[i].setProgress((int) progress);
                    SEEKBAR_LABElS[i].setText((String) animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(i + 1) + "_name") + ": ");

                    isEditListenerWork = false;
                    EDITTEXTS[i].setText(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(i + 1)).toString());
                    isEditListenerWork = true;
            }

        }
        else{

            for (int index = 0;index<listSize-1;index++){
                SEEKBAR_VALUES[index] = ((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).getArgValue(index);
                float progress = ((float) SEEKBAR_VALUES[index] - MIN_VALUES[index]) / RANGE_VALUES[index] * (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL) + MIN_SEEKBAR_VAL;
                SEEKBARS[index].setProgress((int) progress);
                SEEKBAR_LABElS[index].setText(((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).getArgString(index) + ": ");
                isEditListenerWork = false;
                EDITTEXTS[index].setText(String.valueOf(((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).getArgValue(index)));
                isEditListenerWork = true;
            }

            SEEKBAR_VALUES[listSize-1] = Float.valueOf(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(2)).toString());
            float progress = ((float) SEEKBAR_VALUES[listSize-1] - MIN_VALUES[listSize-1]) / RANGE_VALUES[listSize-1] * (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL) + MIN_SEEKBAR_VAL;
            SEEKBARS[listSize-1].setProgress((int) progress);
            SEEKBAR_LABElS[listSize-1].setText((String) animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(2) + "_name") + ": ");
            isEditListenerWork = false;
            EDITTEXTS[listSize-1].setText(animerSolver.getConfigSet().getKeyByString("arg" + String.valueOf(2)).toString());
            isEditListenerWork = true;
        }

    }

    private boolean isEditListenerWork = true;
    private boolean canSetEditText = false;
    private class EditTextListener implements TextWatcher{
        private EditText mEditText;
        private int mIndex;
        public EditTextListener(EditText editText,int index) {
            mEditText = editText;
            mIndex = index;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            String mString = String.valueOf(s);

            if (mString.isEmpty()) {
                //Do Nothing
            } else {
                //Code to perform calculations
                if(isEditListenerWork && isNumeric(mString)){
                    // TODO MIN MAX FIX

                    int mMin = (int) (((0 - MIN_SEEKBAR_VAL) / (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL)) * RANGE_VALUES[mIndex] + MIN_VALUES[mIndex]);
                    int mMax = (int) (((MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL) / (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL)) * RANGE_VALUES[mIndex] + MIN_VALUES[mIndex]);

                    float convertedValue = Float.valueOf(mString);
                    if(convertedValue > (float) mMax){
                        convertedValue = mMax;
                        EDITTEXTS[mIndex].setText(String.valueOf(mMax));
                    }
                    else if(convertedValue < (float) mMin) {
                        convertedValue = mMin;
                        EDITTEXTS[mIndex].setText(String.valueOf(mMin));
                    }

                    float calculatedProgress = (convertedValue - MIN_VALUES[mIndex])/ RANGE_VALUES[mIndex]* (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL) + MIN_SEEKBAR_VAL;
                    canSetEditText = false;
                    SEEKBARS[mIndex].setProgress((int) calculatedProgress);
                    canSetEditText = true;


                }
            }

        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private class SeekbarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int val, boolean b) {

            //TODO Request Renderer

            if(currentObjectType != "AndroidInterpolator") {
                for (int i = 0; i < listSize; i++) {
                    if (seekBar == SEEKBARS[i]) {
                        SEEKBAR_VALUES[i] = ((float) (val - MIN_SEEKBAR_VAL) / (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL)) * RANGE_VALUES[i] + MIN_VALUES[i];
                        if (i == 0) {
                            String roundedValue1Label = DECIMAL_FORMAT_2.format(SEEKBAR_VALUES[i]);
                            SEEKBAR_LABElS[i].setText((String) currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("arg" + String.valueOf(i + 1) + "_name") + ": ");
                            if(canSetEditText){
                                EDITTEXTS[i].setText(roundedValue1Label);
                            }

                            currentAnimer.getCurrentSolver().getConfigSet().addConfig("arg" + String.valueOf(i + 1) + "", Float.valueOf(roundedValue1Label));
                        } else if (i == 1) {
                            String roundedValue1Label = DECIMAL_FORMAT_3.format(SEEKBAR_VALUES[i]);
                            SEEKBAR_LABElS[i].setText((String) currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("arg" + String.valueOf(i + 1) + "_name") + ": ");
                            if(canSetEditText){
                                EDITTEXTS[i].setText(roundedValue1Label);
                            }
                            currentAnimer.getCurrentSolver().getConfigSet().addConfig("arg" + String.valueOf(i + 1) + "", Float.valueOf(roundedValue1Label));
                        }

                    }
                }

                // Seekbar in Fling not works at all
                if (currentObjectType != "AndroidFling") {
                    Object val1 = getConvertValueByIndexAndType(0, currentObjectType);
                    Object val2 = getConvertValueByIndexAndType(1, currentObjectType);
                    currentAnimer.getCurrentSolver().setArg1(val1);
                    currentAnimer.getCurrentSolver().setArg2(val2);
                    float convertVal1 = Float.valueOf(String.valueOf(val1));
                    float convertVal2 = Float.valueOf(String.valueOf(val2));
                    shaderSurfaceView.setCurveMode(1);
                    shaderSurfaceView.setFactorInput(convertVal1,0);
                    shaderSurfaceView.setFactorInput(convertVal2,1);
                }
                else{
                    Object val1 = getConvertValueByIndexAndType(0, currentObjectType);
                    Object val2 = getConvertValueByIndexAndType(1, currentObjectType);
                    float convertVal1 = Float.valueOf(String.valueOf(val1));
                    float convertVal2 = Float.valueOf(String.valueOf(val2));
                    currentAnimer.getCurrentSolver().setArg2(Math.max(0.01f,(float)val2));
                    shaderSurfaceView.setCurveMode(0);
                    shaderSurfaceView.setFactorInput(convertVal1,0);
                    shaderSurfaceView.setFactorInput(convertVal2,1);
                }


            }
            else{

                getCurveModeByString();
                // Interpolator Factor
                for (int i = 0; i < listSize - 1; i++) {
                    if (seekBar == SEEKBARS[i]) {
                        SEEKBAR_VALUES[i] = ((float) (val - MIN_SEEKBAR_VAL) / (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL)) * RANGE_VALUES[i] + MIN_VALUES[i];
                        String roundedValue1Label = DECIMAL_FORMAT_3.format(SEEKBAR_VALUES[i]);
                        SEEKBAR_LABElS[i].setText(((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).getArgString(i) + ": ");
                        if(canSetEditText){
                            EDITTEXTS[i].setText(roundedValue1Label);
                        }
                        ((AnInterpolator) currentAnimer.getCurrentSolver().getArg1()).resetArgValue(i,Float.valueOf(roundedValue1Label));
                        shaderSurfaceView.setFactorInput(Float.valueOf(roundedValue1Label),i);

                        if(currentAnimer.getCurrentSolver().getArg1().getClass().getSimpleName().contains("PathInterpolator")){

                        }

                    }
                }

                // Interpolator Duration
                if (seekBar == SEEKBARS[listSize - 1]) {
                    SEEKBAR_VALUES[listSize - 1] = ((float) (val - MIN_SEEKBAR_VAL) / (MAX_SEEKBAR_VAL - MIN_SEEKBAR_VAL)) * RANGE_VALUES[listSize - 1] + MIN_VALUES[listSize - 1];
                    String roundedValue1Label = DECIMAL_FORMAT_1.format(SEEKBAR_VALUES[listSize - 1]);
                    SEEKBAR_LABElS[listSize - 1].setText((String) currentAnimer.getCurrentSolver().getConfigSet().getKeyByString("arg" + String.valueOf(2) + "_name") + ": ");
                    if(canSetEditText){
                        EDITTEXTS[listSize - 1].setText(roundedValue1Label);
                    }
                    currentAnimer.getCurrentSolver().getConfigSet().addConfig("arg" + String.valueOf(2) + "", Float.valueOf(roundedValue1Label));
                    float floatVal = Float.valueOf(roundedValue1Label);
                    currentAnimer.getCurrentSolver().setArg2( (long) floatVal);
                    shaderSurfaceView.setDuration(floatVal/1000);
                }

            }

            shaderSurfaceView.requestRender();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isEditListenerWork = false;
            canSetEditText = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isEditListenerWork = true;
            canSetEditText = false;
        }
    }

    private String[] interpolatorArray = new String[] {
            "PathInterpolator","LinearInterpolator","AccelerateDecelerateInterpolator","AccelerateInterpolator",
            "DecelerateInterpolator","AnticipateInterpolator","OvershootInterpolator","AnticipateOvershootInterpolator",
            "BounceInterpolator","CycleInterpolator","FastOutSlowInInterpolator","LinearOutSlowInInterpolator",
            "FastOutLinearInInterpolator","CustomMocosSpringInterpolator","CustomSpringInterpolator","CustomBounceInterpolator",
            "CustomDampingInterpolator","AndroidSpringInterpolator"
    };

    private void getCurveModeByString(){

        for(int i=0;i<interpolatorArray.length;i++){
            if(currentAnimer.getCurrentSolver().getArg1().getClass().getSimpleName().equals(interpolatorArray[i])){
                shaderSurfaceView.setCurveMode(2.0f + (i*0.01f));
                break;
            }
        }
    }

    private Object getConvertValueByIndexAndType(int i,String type){
        switch (type) {
            case "NULL":
                return null;
            case "AndroidInterpolator":
                return SEEKBAR_VALUES[i];
            case "AndroidFling":
                return SEEKBAR_VALUES[i];
            case "AndroidSpring":
                return SEEKBAR_VALUES[i];
            case "DHOSpring":
                DHOConverter dhoConverter = new DHOConverter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return dhoConverter.getArg(i);
            case "iOSCoreAnimationSpring":
                DHOConverter iOSCASpring = new DHOConverter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return iOSCASpring.getArg(i);
            case "RK4Spring":
                RK4Converter rk4Converter = new RK4Converter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return rk4Converter.getArg(i);
            case "ProtopieSpring":
                RK4Converter protopieConverter = new RK4Converter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return protopieConverter.getArg(i);
            case "PrincipleSpring":
                RK4Converter principleConverter = new RK4Converter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return principleConverter.getArg(i);
            case "iOSUIViewSpring":
                UIViewSpringConverter uiViewSpringConverter = new UIViewSpringConverter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return uiViewSpringConverter.getArg(i);
            case "OrigamiPOPSpring":
                OrigamiPOPConverter origamiPOPConverter = new OrigamiPOPConverter((float)SEEKBAR_VALUES[0],(float)SEEKBAR_VALUES[1]);
                return origamiPOPConverter.getArg(i);
            default:
                return SEEKBAR_VALUES[i];
        }
    }

    private float nubDragStartY,currViewTransY,nubDragMoveY;

    private boolean setRevealed = false;
    public void setRevealed(boolean boo){
        setRevealed = boo;
    }

    private class OnNubTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            float maxValue = 0;
            float minValue = -(AnConfigView.this.getMeasuredHeight() - getResources().getDimension(R.dimen.nub_height));
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    nubDragStartY = motionEvent.getRawY();
                    currViewTransY = AnConfigView.this.getTranslationY();
                    nubDragMoveY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    nubDragMoveY =  (motionEvent.getRawY() - nubDragStartY);
                    if(nubDragMoveY  + currViewTransY> minValue && nubDragMoveY + currViewTransY< maxValue){
                        mRevealAnimer.setCurrentValue(nubDragMoveY + currViewTransY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if( Math.abs(nubDragMoveY) > (maxValue - minValue)/3){
                        mRevealAnimer.setEndValue((currViewTransY == minValue)?maxValue:minValue);
                    }
                    else{
                        mRevealAnimer.setEndValue((currViewTransY == minValue)?minValue:maxValue);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if( Math.abs(nubDragMoveY) > (maxValue - minValue)/3){
                        mRevealAnimer.setEndValue((currViewTransY == minValue)?maxValue:minValue);
                    }
                    else{
                        mRevealAnimer.setEndValue((currViewTransY == minValue)?minValue:maxValue);
                    }
                    break;
            }

            return true;

        }
    }

    private class OnFPSTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mFPSAnimer.setEndValue(0.8f);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mFPSAnimer.setEndValue(1f);
                    if (String.valueOf(fpsView.getText()).contains("FPS")) {
                        FPSDetector.create().addFrameDataCallback(new FrameDataCallback() {
                            @Override
                            public void doFrame(long previousFrameNS, long currentFrameNS, int droppedFrames, float currentFPS) {
                                if(currentFPS <50 && currentFPS > 30){
                                    fpsView.setTextColor(Color.YELLOW);
                                }
                                else if(currentFPS>=50){
                                    fpsView.setTextColor(Color.GREEN);
                                }
                                else if(currentFPS < 30){
                                    fpsView.setTextColor(Color.RED);
                                }
                                fpsView.setText(String.valueOf(currentFPS));
                            }
                        }).show(mContext);
                    } else {
                        FPSDetector.hide(mContext);
                        fpsView.setTextColor(backgroundColor);
                        fpsView.setText("FPS");
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mFPSAnimer.setEndValue(1);
                    if (String.valueOf(fpsView.getText()).contains("FPS")) {
                        FPSDetector.create().addFrameDataCallback(new FrameDataCallback() {
                            @Override
                            public void doFrame(long previousFrameNS, long currentFrameNS, int droppedFrames, float currentFPS) {
                                if(currentFPS <50 && currentFPS > 30){
                                    fpsView.setTextColor(Color.YELLOW);
                                }
                                else if(currentFPS>=50){
                                    fpsView.setTextColor(Color.GREEN);
                                }
                                else if(currentFPS < 30){
                                    fpsView.setTextColor(Color.RED);
                                }
                                fpsView.setText(String.valueOf(currentFPS));
                            }
                        }).show(mContext);
                    } else {
                        FPSDetector.hide(mContext);
                        fpsView.setTextColor(backgroundColor);
                        fpsView.setText("FPS");
                    }
                    break;
            }
            return true;
        }
    }

    public static int dpToPx(float dp, Resources res) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,res.getDisplayMetrics());
    }

    public static FrameLayout.LayoutParams createLayoutParams(int width, int height) {
        return new FrameLayout.LayoutParams(width, height);
    }
}
