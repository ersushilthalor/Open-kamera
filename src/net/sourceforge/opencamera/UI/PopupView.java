package net.sourceforge.opencamera.UI;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.Preview;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.CameraController.CameraController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PopupView extends LinearLayout {
	private static final String TAG = "PopupView";

	private int picture_size_index = -1;
	private int video_size_index = -1;
	private int timer_index = -1;
	private int burst_mode_index = -1;

	private Map<String, View> popup_buttons = new Hashtable<String, View>();

	public PopupView(Context context) {
		super(context);

		this.setOrientation(LinearLayout.VERTICAL);
		final float scale = getResources().getDisplayMetrics().density;
		int pad = (int)(10 * scale + 0.5f);
		this.setPadding(pad, pad, pad, pad);

		// Header title
		TextView headerView = new TextView(this.getContext());
		headerView.setText("QUICK CONTROLS");
		headerView.setTextColor(Color.parseColor("#A8C7FA"));
		headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
		headerView.setTypeface(Typeface.DEFAULT_BOLD);
		headerView.setGravity(Gravity.CENTER);
		int headerPad = (int)(4 * scale + 0.5f);
		headerView.setPadding(0, 0, 0, headerPad);
		this.addView(headerView);

		final MainActivity main_activity = (MainActivity)this.getContext();
		final Preview preview = main_activity.getPreview();
        List<String> supported_flash_values = preview.getSupportedFlashValues();
    	addButtonOptionsToPopup(supported_flash_values, R.array.flash_icons, R.array.flash_values, getResources().getString(R.string.flash_mode), preview.getCurrentFlashValue(), "TEST_FLASH", new ButtonOptionsPopupListener() {
			@Override
			public void onClick(String option) {
				if( MyDebug.LOG )
					Log.d(TAG, "clicked flash: " + option);
				preview.updateFlash(option);
		    	main_activity.setPopupIcon();
				main_activity.closePopup();
			}
		});
    	
		if( preview.isVideo() && preview.isTakingPhoto() ) {
    		// don't add any more options
    	}
    	else {
        	List<String> supported_focus_values = preview.getSupportedFocusValues();
        	addButtonOptionsToPopup(supported_focus_values, R.array.focus_mode_icons, R.array.focus_mode_values, getResources().getString(R.string.focus_mode), preview.getCurrentFocusValue(), "TEST_FOCUS", new ButtonOptionsPopupListener() {
    			@Override
    			public void onClick(String option) {
    				if( MyDebug.LOG )
    					Log.d(TAG, "clicked focus: " + option);
    				preview.updateFocus(option, false, true);
    				main_activity.closePopup();
    			}
    		});
            
    		List<String> supported_isos = preview.getSupportedISOs();
    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
    		String current_iso = sharedPreferences.getString(MainActivity.getISOPreferenceKey(), "auto");
        	addButtonOptionsToPopup(supported_isos, -1, -1, "ISO", current_iso, "TEST_ISO", new ButtonOptionsPopupListener() {
    			@Override
    			public void onClick(String option) {
    				if( MyDebug.LOG )
    					Log.d(TAG, "clicked iso: " + option);
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
    				SharedPreferences.Editor editor = sharedPreferences.edit();
    				editor.putString(MainActivity.getISOPreferenceKey(), option);
    				editor.apply();

    				main_activity.updateForSettings("ISO: " + option);
    				main_activity.closePopup();
    			}
    		});

    		if( preview.getCameraController() != null ) {
	        	List<String> supported_white_balances = preview.getSupportedWhiteBalances();
	        	addRadioOptionsToPopup(supported_white_balances, getResources().getString(R.string.white_balance), MainActivity.getWhiteBalancePreferenceKey(), preview.getCameraController().getDefaultWhiteBalance(), "TEST_WHITE_BALANCE");
	
	        	List<String> supported_scene_modes = preview.getSupportedSceneModes();
	        	addRadioOptionsToPopup(supported_scene_modes, getResources().getString(R.string.scene_mode), MainActivity.getSceneModePreferenceKey(), preview.getCameraController().getDefaultSceneMode(), "TEST_SCENE_MODE");
	
	        	List<String> supported_color_effects = preview.getSupportedColorEffects();
	        	addRadioOptionsToPopup(supported_color_effects, getResources().getString(R.string.color_effect), MainActivity.getColorEffectPreferenceKey(), preview.getCameraController().getDefaultColorEffect(), "TEST_COLOR_EFFECT");
    		}
        	
        	if( main_activity.supportsAutoStabilise() ) {
        		CheckBox checkBox = new CheckBox(main_activity);
        		checkBox.setText(getResources().getString(R.string.preference_auto_stabilise));
        		checkBox.setTextColor(Color.WHITE);
        		checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.0f);
        		int padBox = (int)(6 * scale + 0.5f);
        		checkBox.setPadding(padBox, padBox, padBox, padBox);

        		boolean auto_stabilise = sharedPreferences.getBoolean(MainActivity.getAutoStabilisePreferenceKey(), false);
        		checkBox.setChecked(auto_stabilise);
        		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
	    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean(MainActivity.getAutoStabilisePreferenceKey(), isChecked);
						editor.apply();

						String message = getResources().getString(R.string.preference_auto_stabilise) + ": " + getResources().getString(isChecked ? R.string.on : R.string.off);
						preview.showToast(main_activity.getChangedAutoStabiliseToastBoxer(), message);
						main_activity.closePopup();
					}
        		});

        		this.addView(checkBox);
        	}

    		final List<CameraController.Size> picture_sizes = preview.getSupportedPictureSizes();
    		picture_size_index = preview.getCurrentPictureSizeIndex();
    		final List<String> picture_size_strings = new ArrayList<String>();
    		for(CameraController.Size picture_size : picture_sizes) {
    			String size_string = picture_size.width + " x " + picture_size.height + " " + Preview.getMPString(picture_size.width, picture_size.height);
    			picture_size_strings.add(size_string);
    		}
    		addArrayOptionsToPopup(picture_size_strings, getResources().getString(R.string.preference_resolution), picture_size_index, new ArrayOptionsPopupListener() {
		    	final Handler handler = new Handler();
				Runnable update_runnable = new Runnable() {
					@Override
					public void run() {
						if( MyDebug.LOG )
							Log.d(TAG, "update settings due to resolution change");
						main_activity.updateForSettings("");
					}
				};

				private void update() {
    				if( picture_size_index == -1 )
    					return;
    				CameraController.Size new_size = picture_sizes.get(picture_size_index);
	                String resolution_string = new_size.width + " " + new_size.height;
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(MainActivity.getResolutionPreferenceKey(preview.getCameraId()), resolution_string);
					editor.apply();

					handler.removeCallbacks(update_runnable);
					handler.postDelayed(update_runnable, 400);
    			}
				@Override
				public int onClickPrev() {
	        		if( picture_size_index != -1 && picture_size_index > 0 ) {
	        			picture_size_index--;
	        			update();
	    				return picture_size_index;
	        		}
					return -1;
				}
				@Override
				public int onClickNext() {
	                if( picture_size_index != -1 && picture_size_index < picture_sizes.size()-1 ) {
	                	picture_size_index++;
	        			update();
	    				return picture_size_index;
	        		}
					return -1;
				}
    		});

    		final List<String> video_sizes = preview.getSupportedVideoQuality();
    		video_size_index = preview.getCurrentVideoQualityIndex();
    		final List<String> video_size_strings = new ArrayList<String>();
    		for(String video_size : video_sizes) {
    			String quality_string = preview.getCamcorderProfileDescriptionShort(video_size);
    			video_size_strings.add(quality_string);
    		}
    		addArrayOptionsToPopup(video_size_strings, getResources().getString(R.string.video_quality), video_size_index, new ArrayOptionsPopupListener() {
		    	final Handler handler = new Handler();
				Runnable update_runnable = new Runnable() {
					@Override
					public void run() {
						if( MyDebug.LOG )
							Log.d(TAG, "update settings due to video resolution change");
						main_activity.updateForSettings("");
					}
				};

				private void update() {
    				if( video_size_index == -1 )
    					return;
    				String quality = video_sizes.get(video_size_index);
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(MainActivity.getVideoQualityPreferenceKey(preview.getCameraId()), quality);
					editor.apply();

					handler.removeCallbacks(update_runnable);
					handler.postDelayed(update_runnable, 400);
    			}
				@Override
				public int onClickPrev() {
	        		if( video_size_index != -1 && video_size_index > 0 ) {
	        			video_size_index--;
	        			update();
	    				return video_size_index;
	        		}
					return -1;
				}
				@Override
				public int onClickNext() {
	                if( video_size_index != -1 && video_size_index < video_sizes.size()-1 ) {
	                	video_size_index++;
	        			update();
	    				return video_size_index;
	        		}
					return -1;
				}
    		});

    		final String [] timer_values = getResources().getStringArray(R.array.preference_timer_values);
        	String [] timer_entries = getResources().getStringArray(R.array.preference_timer_entries);
    		String timer_value = sharedPreferences.getString(MainActivity.getTimerPreferenceKey(), "0");
    		timer_index = Arrays.asList(timer_values).indexOf(timer_value);
    		if( timer_index == -1 ) {
				if( MyDebug.LOG )
					Log.d(TAG, "can't find timer_value " + timer_value + " in timer_values!");
				timer_index = 0;
    		}
    		addArrayOptionsToPopup(Arrays.asList(timer_entries), getResources().getString(R.string.preference_timer), timer_index, new ArrayOptionsPopupListener() {
    			private void update() {
    				if( timer_index == -1 )
    					return;
    				String new_timer_value = timer_values[timer_index];
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(MainActivity.getTimerPreferenceKey(), new_timer_value);
					editor.apply();
    			}
				@Override
				public int onClickPrev() {
	        		if( timer_index != -1 && timer_index > 0 ) {
	        			timer_index--;
	        			update();
	    				return timer_index;
	        		}
					return -1;
				}
				@Override
				public int onClickNext() {
	                if( timer_index != -1 && timer_index < timer_values.length-1 ) {
	                	timer_index++;
	        			update();
	    				return timer_index;
	        		}
					return -1;
				}
    		});

        	final String [] burst_mode_values = getResources().getStringArray(R.array.preference_burst_mode_values);
        	String [] burst_mode_entries = getResources().getStringArray(R.array.preference_burst_mode_entries);
    		String burst_mode_value = sharedPreferences.getString(MainActivity.getBurstModePreferenceKey(), "1");
    		burst_mode_index = Arrays.asList(burst_mode_values).indexOf(burst_mode_value);
    		if( burst_mode_index == -1 ) {
				if( MyDebug.LOG )
					Log.d(TAG, "can't find burst_mode_value " + burst_mode_value + " in burst_mode_values!");
				burst_mode_index = 0;
    		}
    		addArrayOptionsToPopup(Arrays.asList(burst_mode_entries), getResources().getString(R.string.preference_burst_mode), burst_mode_index, new ArrayOptionsPopupListener() {
    			private void update() {
    				if( burst_mode_index == -1 )
    					return;
    				String new_burst_mode_value = burst_mode_values[burst_mode_index];
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(MainActivity.getBurstModePreferenceKey(), new_burst_mode_value);
					editor.apply();
    			}
				@Override
				public int onClickPrev() {
	        		if( burst_mode_index != -1 && burst_mode_index > 0 ) {
	        			burst_mode_index--;
	        			update();
	    				return burst_mode_index;
	        		}
					return -1;
				}
				@Override
				public int onClickNext() {
	                if( burst_mode_index != -1 && burst_mode_index < burst_mode_values.length-1 ) {
	                	burst_mode_index++;
	        			update();
	    				return burst_mode_index;
	        		}
					return -1;
				}
    		});
    	}
	}

    private abstract class ButtonOptionsPopupListener {
		public abstract void onClick(String option);
    }
    
    private void addButtonOptionsToPopup(List<String> supported_options, int icons_id, int values_id, String string, String current_value, String test_key, final ButtonOptionsPopupListener listener) {
		if( MyDebug.LOG )
			Log.d(TAG, "addButtonOptionsToPopup");
    	if( supported_options != null ) {
			final float scale = getResources().getDisplayMetrics().density;

			// Add category title
			TextView titleView = new TextView(this.getContext());
			titleView.setText(string.toUpperCase());
			titleView.setTextColor(Color.parseColor("#A8C7FA"));
			titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.0f);
			titleView.setTypeface(Typeface.DEFAULT_BOLD);
			titleView.setPadding(0, (int)(10 * scale + 0.5f), 0, (int)(4 * scale + 0.5f));
			this.addView(titleView);

        	LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);
        	String [] icons = icons_id != -1 ? getResources().getStringArray(icons_id) : null;
        	String [] values = values_id != -1 ? getResources().getStringArray(values_id) : null;

			int total_width = 300;
			{
				Activity activity = (Activity)this.getContext();
			    Display display = activity.getWindowManager().getDefaultDisplay();
			    DisplayMetrics outMetrics = new DisplayMetrics();
			    display.getMetrics(outMetrics);

			    int dpHeight = (int)(outMetrics.heightPixels / scale);
    			dpHeight -= 60;
    			if( total_width > dpHeight )
    				total_width = dpHeight;
			}
			int button_width_dp = total_width/supported_options.size();
			boolean use_scrollview = false;
			if( button_width_dp < 46 ) {
				button_width_dp = 46;
				use_scrollview = true;
			}
			View current_view = null;

			for(final String supported_option : supported_options) {
        		int resource = -1;
        		if( icons != null && values != null ) {
            		int index = -1;
            		for(int i=0;i<values.length && index==-1;i++) {
            			if( values[i].equals(supported_option) )
            				index = i;
            		}
            		if( index != -1 ) {
            			resource = getResources().getIdentifier(icons[index], null, this.getContext().getApplicationContext().getPackageName());
            		}
        		}

        		View view = null;
				boolean is_selected = supported_option.equals(current_value);
        		if( resource != -1 ) {
        			ImageButton image_button = new ImageButton(this.getContext());
        			view = image_button;
        			ll2.addView(view);

        			final MainActivity main_activity = (MainActivity)this.getContext();
        			Bitmap bm = main_activity.getPreloadedBitmap(resource);
        			if( bm != null )
        				image_button.setImageBitmap(bm);
        			else {
        				image_button.setImageResource(resource);
        			}
        			image_button.setScaleType(ScaleType.FIT_CENTER);
        			final int padding = (int) (10 * scale + 0.5f);
        			view.setPadding(padding, padding, padding, padding);
        		}
        		else {
        			Button button = new Button(this.getContext());
        			view = button;
        			ll2.addView(view);

        			if( string.equalsIgnoreCase("ISO") && supported_option.length() >= 4 && supported_option.substring(0, 4).equalsIgnoreCase("ISO_") ) {
            			button.setText(string + "\n" + supported_option.substring(4));
        			}
        			else if( string.equalsIgnoreCase("ISO") && supported_option.length() >= 3 && supported_option.substring(0, 3).equalsIgnoreCase("ISO") ) {
            			button.setText(string + "\n" + supported_option.substring(3));
        			}
        			else {
            			button.setText(string + "\n" + supported_option);
        			}
        			button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
					button.setTypeface(Typeface.DEFAULT_BOLD);
        			button.setTextColor(is_selected ? Color.parseColor("#062E6F") : Color.WHITE);
        			view.setPadding(0, 0, 0, 0);
        		}

    			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					(int) (button_width_dp * scale + 0.5f),
					(int) (44 * scale + 0.5f)
				);
				int marginPx = (int)(2 * scale + 0.5f);
				params.setMargins(marginPx, 0, marginPx, 0);
    			view.setLayoutParams(params);

    			view.setContentDescription(string);
    			if( is_selected ) {
    				view.setBackgroundResource(R.drawable.bg_chip_selected);
    				view.setAlpha(1.0f);
    				current_view = view;
    			}
    			else {
    				view.setBackgroundResource(R.drawable.bg_chip_unselected);
    				view.setAlpha(0.9f);
    			}

    			view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if( MyDebug.LOG )
							Log.d(TAG, "clicked: " + supported_option);
						listener.onClick(supported_option);
					}
    			});
    			this.popup_buttons.put(test_key + "_" + supported_option, view);
    		}

			if( use_scrollview ) {
	        	final HorizontalScrollView scroll = new HorizontalScrollView(this.getContext());
				scroll.setHorizontalScrollBarEnabled(false);
	        	scroll.addView(ll2);
	        	{
	    			ViewGroup.LayoutParams params = new LayoutParams(
	    					(int) (total_width * scale + 0.5f),
	    			        LayoutParams.WRAP_CONTENT);
	    			scroll.setLayoutParams(params);
	        	}
	        	this.addView(scroll);
	        	if( current_view != null ) {
	        		final View final_current_view = current_view;
	        		this.getViewTreeObserver().addOnGlobalLayoutListener( 
	        			new OnGlobalLayoutListener() {
							@Override
							public void onGlobalLayout() {
				        		scroll.scrollTo(final_current_view.getLeft(), 0);
							}
	        			}
	        		);
	        	}
			}
			else {
	    		this.addView(ll2);
			}
        }
    }
    
    private void addRadioOptionsToPopup(List<String> supported_options, final String title, final String preference_key, final String default_option, final String test_key) {
		if( MyDebug.LOG )
			Log.d(TAG, "addOptionsToPopup: " + title);
    	if( supported_options != null ) {
    		final MainActivity main_activity = (MainActivity)this.getContext();
			final float scale = getResources().getDisplayMetrics().density;

    		TextView text_view = new TextView(this.getContext());
    		text_view.setText(title.toUpperCase());
    		text_view.setTextColor(Color.parseColor("#A8C7FA"));
    		text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.0f);
			text_view.setTypeface(Typeface.DEFAULT_BOLD);
			text_view.setPadding(0, (int)(10 * scale + 0.5f), 0, (int)(4 * scale + 0.5f));
        	this.addView(text_view);

    		RadioGroup rg = new RadioGroup(this.getContext()); 
        	rg.setOrientation(RadioGroup.VERTICAL);
        	this.popup_buttons.put(test_key, rg);

			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
			String current_option = sharedPreferences.getString(preference_key, default_option);
        	for(final String supported_option : supported_options) {
        		RadioButton button = new RadioButton(this.getContext());
        		button.setText(supported_option);
        		button.setTextColor(Color.WHITE);
        		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.0f);
        		int padR = (int)(6 * scale + 0.5f);
        		button.setPadding(padR, padR, padR, padR);
        		if( supported_option.equals(current_option) ) {
        			button.setChecked(true);
        		}
        		else {
        			button.setChecked(false);
        		}
    			rg.addView(button);
    			button.setContentDescription(supported_option);
    			button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if( MyDebug.LOG )
							Log.d(TAG, "clicked current_option: " + supported_option);
						SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString(preference_key, supported_option);
						editor.apply();

						main_activity.updateForSettings(title + ": " + supported_option);
						main_activity.closePopup();
					}
    			});
    			this.popup_buttons.put(test_key + "_" + supported_option, button);
        	}
        	this.addView(rg);
        }
    }
    
    private abstract class ArrayOptionsPopupListener {
		public abstract int onClickPrev();
		public abstract int onClickNext();
    }
    
    private void addArrayOptionsToPopup(final List<String> supported_options, final String title, final int current_index, final ArrayOptionsPopupListener listener) {
		if( supported_options != null && current_index != -1 ) {
			final float scale = getResources().getDisplayMetrics().density;

    		TextView text_view = new TextView(this.getContext());
    		text_view.setText(title.toUpperCase());
    		text_view.setTextColor(Color.parseColor("#A8C7FA"));
    		text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.0f);
			text_view.setTypeface(Typeface.DEFAULT_BOLD);
			text_view.setPadding(0, (int)(10 * scale + 0.5f), 0, (int)(4 * scale + 0.5f));
        	this.addView(text_view);

        	LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);
			ll2.setGravity(Gravity.CENTER_VERTICAL);
			ll2.setBackgroundResource(R.drawable.bg_pill_surface);
			int padHoriz = (int)(4 * scale + 0.5f);
			int padVert = (int)(2 * scale + 0.5f);
			ll2.setPadding(padHoriz, padVert, padHoriz, padVert);
            
			final TextView resolution_text_view = new TextView(this.getContext());
			resolution_text_view.setText(supported_options.get(current_index));
			resolution_text_view.setTextColor(Color.WHITE);
			resolution_text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
			resolution_text_view.setTypeface(Typeface.DEFAULT_BOLD);
			resolution_text_view.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
			resolution_text_view.setLayoutParams(params);

			final Button prev_button = new Button(this.getContext());
			ll2.addView(prev_button);
			prev_button.setText("‹");
			prev_button.setTextColor(Color.parseColor("#A8C7FA"));
			prev_button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
			prev_button.setBackgroundResource(R.drawable.bg_circle_button);
			prev_button.setPadding(0, 0, 0, 0);
			ViewGroup.LayoutParams vg_params = prev_button.getLayoutParams();
			vg_params.width = (int) (44 * scale + 0.5f);
			vg_params.height = (int) (40 * scale + 0.5f);
			prev_button.setLayoutParams(vg_params);
			prev_button.setVisibility( (current_index > 0) ? View.VISIBLE : View.INVISIBLE);

        	ll2.addView(resolution_text_view);

			final Button next_button = new Button(this.getContext());
			ll2.addView(next_button);
			next_button.setText("›");
			next_button.setTextColor(Color.parseColor("#A8C7FA"));
			next_button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
			next_button.setBackgroundResource(R.drawable.bg_circle_button);
			next_button.setPadding(0, 0, 0, 0);
			vg_params = next_button.getLayoutParams();
			vg_params.width = (int) (44 * scale + 0.5f);
			vg_params.height = (int) (40 * scale + 0.5f);
			next_button.setLayoutParams(vg_params);
			next_button.setVisibility( (current_index < supported_options.size()-1) ? View.VISIBLE : View.INVISIBLE);

			prev_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
        			int new_index = listener.onClickPrev();
        			if( new_index != -1 ) {
        				resolution_text_view.setText(supported_options.get(new_index));
	        			prev_button.setVisibility( (new_index > 0) ? View.VISIBLE : View.INVISIBLE);
	        			next_button.setVisibility( (new_index < supported_options.size()-1) ? View.VISIBLE : View.INVISIBLE);
        			}
				}
			});
			next_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
        			int new_index = listener.onClickNext();
        			if( new_index != -1 ) {
        				resolution_text_view.setText(supported_options.get(new_index));
	        			prev_button.setVisibility( (new_index > 0) ? View.VISIBLE : View.INVISIBLE);
	        			next_button.setVisibility( (new_index < supported_options.size()-1) ? View.VISIBLE : View.INVISIBLE);
        			}
				}
			});

			this.addView(ll2);
    	}
    }

    public void close() {
		if( MyDebug.LOG )
			Log.d(TAG, "close");
		popup_buttons.clear();
    }

    // for testing
    public View getPopupButton(String key) {
    	return popup_buttons.get(key);
    }
}
