package com.android.videonatic.customview;







import com.android.videonatic.R;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgress extends Dialog {
	private static String text;

	public static CustomProgress show(Context context, CharSequence title,
	        CharSequence message) {
	    return show(context, title, message, false);
	}

	public static CustomProgress show(Context context, CharSequence title,
	        CharSequence message, boolean indeterminate) {
	    return show(context, title, message, indeterminate, false, null);
	}

	public static CustomProgress show(Context context, CharSequence title,
	        CharSequence message, boolean indeterminate, boolean cancelable) {
	    return show(context, title, message, indeterminate, cancelable, null);
	}

	public static CustomProgress show(Context context, CharSequence title,
	        CharSequence message, boolean indeterminate,
	        boolean cancelable, OnCancelListener cancelListener) {
	        CustomProgress dialog = new CustomProgress(context, text);
		    dialog.setTitle(title);
		    dialog.setCancelable(cancelable);
		    dialog.setOnCancelListener(cancelListener);
		    
		    /* The next line will add the ProgressBar to the dialog. */
		    dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		    dialog.show();

	    return dialog;
	}

	public CustomProgress(Context context, String text) {
	    super(context, R.style.NewDialog);
	    setContentView(R.layout.custom_progress_dialog);
	    TextView progress_text = (TextView) findViewById(R.id.MyProgressDialogTextView);
		progress_text.setText(text);
	}
	
	public CustomProgress(Context context, int text) {
	    super(context, R.style.NewDialog);
	    setContentView(R.layout.custom_progress_dialog);
	    TextView progress_text = (TextView) findViewById(R.id.MyProgressDialogTextView);
		progress_text.setText(text);
	}

}
