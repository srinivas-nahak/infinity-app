package custom_views_and_styles;

/**
 * Created by Srinu on 07-03-2018.
 */

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public final class HashTagHelper implements ClickableForegroundColorSpan.OnHashTagClickListener {


    private final List<Character> mAdditionalHashTagChars;
    private TextView mTextView;
    private int mHashTagWordColor;

    private OnHashTagClickListener mOnHashTagClickListener;

    public static final class Creator{

        private Creator(){}

        public static HashTagHelper create(int color, OnHashTagClickListener listener){
            return new HashTagHelper(color, listener, null);
        }

        public static HashTagHelper create(int color, OnHashTagClickListener listener, char... additionalHashTagChars){
            return new HashTagHelper(color, listener, additionalHashTagChars);
        }

    }

    public interface OnHashTagClickListener{
        void onHashTagClicked(String hashTag,char sign);
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            if (text.length() > 0) {
                eraseAndColorizeAllText(text);
                //if(suggestionBox!=null) Toast.makeText(suggestionBox.getContext(),"Not HashTag",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private HashTagHelper(int color, OnHashTagClickListener listener, char... additionalHashTagCharacters) {
        mHashTagWordColor = color;
        mOnHashTagClickListener = listener;
        mAdditionalHashTagChars = new ArrayList<>();

        if(additionalHashTagCharacters != null){
            for(char additionalChar : additionalHashTagCharacters){
                mAdditionalHashTagChars.add(additionalChar);
            }
        }
    }

    public void handle(TextView textView){
        if(mTextView == null){
            mTextView = textView;
            mTextView.addTextChangedListener(mTextWatcher);

            // in order to use spannable we have to set buffer type
            mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);

            if(mOnHashTagClickListener != null){
                // we need to set this in order to get onClick event
                mTextView.setMovementMethod(LinkMovementMethod.getInstance());

                // after onClick clicked text become highlighted
                mTextView.setHighlightColor(Color.TRANSPARENT);
            } else {
                // hash tags are not clickable, no need to change these parameters
            }

            setColorsToAllHashTags(mTextView.getText());
        } else {
            throw new RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView");
        }

    }

    private void eraseAndColorizeAllText(CharSequence text) {

        Spannable spannable = ((Spannable) mTextView.getText());

        CharacterStyle[] spans = spannable.getSpans(0, text.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            spannable.removeSpan(span);
        }

        setColorsToAllHashTags(text);
    }

    private void setColorsToAllHashTags(CharSequence text) {

        int startIndexOfNextHashSign;

        int index = 0;
        while (index < text.length()-  1){
            char sign = text.charAt(index);
            int nextNotLetterDigitCharIndex = index + 1; // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if(sign == '@'||sign=='#'){

                /*if(suggestionBox!=null) {
                    //suggestionBox.setVisibility(View.VISIBLE);
                    JSONObject obj=new JSONObject();
                    try {
                             socket.on("search",handleSearch);
                            //String result = charSequence.toString().replaceAll("\\\\@","");
                            obj.put("search", text.subSequence(index+1,text.length()));
                            socket.emit("data", obj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                startIndexOfNextHashSign = index;

                nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);

                setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex);
            }
            index = nextNotLetterDigitCharIndex;
        }
    }

    private int findNextValidHashTagChar(CharSequence text, int start) {

        int nonLetterDigitCharIndex = -1; // skip first sign '#"
        for (int index = start + 1; index < text.length(); index++) {

            char sign = text.charAt(index);

            boolean isValidSign = Character.isLetterOrDigit(sign) || mAdditionalHashTagChars.contains(sign);
            if (!isValidSign) {
                nonLetterDigitCharIndex = index;
                break;
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length();
        }

        return nonLetterDigitCharIndex;
    }

    private void setColorForHashTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex) {
        Spannable s = (Spannable) mTextView.getText();

        CharacterStyle span;

        if(mOnHashTagClickListener != null){
            span = new ClickableForegroundColorSpan(mHashTagWordColor, this);
        } else {
            // no need for clickable span because it is messing with selection when click
            span = new ForegroundColorSpan(mHashTagWordColor);
        }

        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public List<String> getAllTags(boolean withHashes) {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText();

        // use set to exclude duplicates
        Set<String> hashTags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            hashTags.add(
                    text.substring(!withHashes ? spannable.getSpanStart(span) + 1/*skip "#" sign*/
                                    : spannable.getSpanStart(span),
                            spannable.getSpanEnd(span)));
        }

        return new ArrayList<>(hashTags);
    }
    public List<String> getOnlyTags(boolean withHashes,String tag) {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText();

        // use set to exclude duplicates
        Set<String> tags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            if(text.substring(spannable.getSpanStart(span),spannable.getSpanStart(span)+1).equals(tag))
                tags.add(
                        text.substring(!withHashes ? spannable.getSpanStart(span) + 1/*skip "#" or "@" sign*/
                                        : spannable.getSpanStart(span),
                                spannable.getSpanEnd(span)));
        }

        return new ArrayList<>(tags);
    }
    public List<String> getAllTags() {
        return getAllTags(false);
    }

    @Override
    public void onHashTagClicked(String hashTag,char sign) {
        mOnHashTagClickListener.onHashTagClicked(hashTag,sign);
    }
}

