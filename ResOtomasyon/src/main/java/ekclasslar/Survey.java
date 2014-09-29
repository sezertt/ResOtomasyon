package ekclasslar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.res_otomasyon.resotomasyon.GlobalApplication;
import com.res_otomasyon.resotomasyon.R;

/**
 * Created by Mustafa on 23.9.2014.
 */
public class Survey {

    Activity activity;
    GlobalApplication g;
    Context context;

    public Survey(Activity activity, GlobalApplication globalApplication) {
        this.activity = activity;
        this.g = globalApplication;
        this.context = activity;
    }

    public void createSurvey(final String[] questionsAndPlaces) {

        final TableLayout tableLayout = (TableLayout) activity.findViewById(R.id.tableLayout);

        final String[] onlyQuestions = new String[questionsAndPlaces.length];
        final String[] onlyPlaces = new String[questionsAndPlaces.length];

        for (int i = 0; i < questionsAndPlaces.length; i++) {
            String[] questionAndPlacesSide = questionsAndPlaces[i].split("\\-");

            onlyQuestions[i] = questionAndPlacesSide[0];
            onlyPlaces[i] = questionAndPlacesSide[1];

            TableRow tr = new TableRow(activity);

            TableRow.LayoutParams layoutTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1);

            tr.setLayoutParams(layoutTableRowParams);

            TableRow.LayoutParams layoutTextViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1);

            layoutTextViewParams.setMargins(10, 0, 0, 0);

            TextView textQuestionView = new TextView(activity);
            textQuestionView.setTextAppearance(activity, android.R.style.TextAppearance_Large);
            textQuestionView.setText((i + 1) + " - " + questionAndPlacesSide[0]);
            textQuestionView.setSingleLine(false);
            textQuestionView.setMaxLines(3);
            textQuestionView.setGravity(Gravity.LEFT | Gravity.CENTER);
            textQuestionView.setLines(3);
            textQuestionView.setLayoutParams(layoutTextViewParams);
            tr.addView(textQuestionView);

            if (Integer.parseInt(questionAndPlacesSide[1]) < 16) // seçmeli soru rating bar ve text
            {
                TableRow.LayoutParams layoutRatingBarParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                layoutRatingBarParams.setMargins(0, 0, 10, 0);

                RatingBar ratingBarView = new RatingBar(activity);
                ratingBarView.setTag("" + questionAndPlacesSide[1]);
                ratingBarView.setNumStars(5);
                ratingBarView.setStepSize(1);
                ratingBarView.setRating(1);
                layoutRatingBarParams.gravity = (Gravity.RIGHT | Gravity.CENTER);
                ratingBarView.setLayoutParams(layoutRatingBarParams);
                tr.addView(ratingBarView);
                tableLayout.addView(tr);
            } else // yazılı soru text ve cevap text
            {
                tableLayout.addView(tr);

                TableRow.LayoutParams layoutEditTextParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
                layoutEditTextParams.setMargins(10, 10, 10, 0);

                EditText editText = new EditText(activity);
                editText.setSingleLine(false);
                editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                editText.setMinLines(8);
                editText.setLines(8);
                editText.setGravity(Gravity.TOP | Gravity.LEFT);
                editText.setBackgroundResource(R.drawable.buttonstyle);
                editText.setLayoutParams(layoutEditTextParams);
                editText.setTag("" + questionAndPlacesSide[1]);


                InputFilter[] filters = new InputFilter[2];
                filters[0] = new InputFilter.LengthFilter(500);
                filters[1] = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {

                        if (source instanceof SpannableStringBuilder) {
                            SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
                            for (int i = end - 1; i >= start; i--) {
                                char currentChar = source.charAt(i);
                                if (currentChar == '<' && currentChar == '>'&& currentChar == '&'&& currentChar == '='&& currentChar == '*'&& currentChar == '-') {
                                    sourceAsSpannableBuilder.delete(i, i + 1);
                                }
                            }
                            return source;
                        } else {
                            StringBuilder filteredStringBuilder = new StringBuilder();
                            for (int i = start; i < end; i++) {
                                char currentChar = source.charAt(i);
                                if (currentChar != '<' && currentChar != '>'&& currentChar != '&'&& currentChar != '='&& currentChar != '*'&& currentChar != '-') {
                                    filteredStringBuilder.append(currentChar);
                                }
                            }
                            return filteredStringBuilder.toString();
                        }
                    }
                };

                editText.setFilters(filters);

                TableRow tr2 = new TableRow(activity);

                TableRow.LayoutParams layoutTableRowParams2 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1);

                tr2.setLayoutParams(layoutTableRowParams2);
                tr2.addView(editText);
                tableLayout.addView(tr2);
            }
        }

        TableRow tr = new TableRow(activity);

        TableRow.LayoutParams layoutTableRowParams2 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1);

        tr.setLayoutParams(layoutTableRowParams2);

        for (int i = 0; i < 2; i++) {
            TableRow.LayoutParams layoutEditTextParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
            layoutEditTextParams.setMargins(10, 10, 10, 10);

            EditText editText = new EditText(activity);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText.setLayoutParams(layoutEditTextParams);

            if (i == 0)
                editText.setHint("Ad *");
            else
                editText.setHint("Soyad *");

            editText.setTextSize(25);
            editText.setTag("" + (i + 20));

            InputFilter[] filters = new InputFilter[2];
            filters[0] = new InputFilter.LengthFilter(63);
            filters[1] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {

                    if (source instanceof SpannableStringBuilder) {
                        SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
                        for (int i = end - 1; i >= start; i--) {
                            char currentChar = source.charAt(i);
                            if (currentChar == '<' && currentChar == '>'&& currentChar == '&'&& currentChar == '='&& currentChar == '*'&& currentChar == '-') {
                                sourceAsSpannableBuilder.delete(i, i + 1);
                            }
                        }
                        return source;
                    } else {
                        StringBuilder filteredStringBuilder = new StringBuilder();
                        for (int i = start; i < end; i++) {
                            char currentChar = source.charAt(i);
                            if (currentChar != '<' && currentChar != '>'&& currentChar != '&'&& currentChar != '='&& currentChar != '*'&& currentChar != '-') {
                                filteredStringBuilder.append(currentChar);
                            }
                        }
                        return filteredStringBuilder.toString();
                    }
                }
            };
            editText.setFilters(filters);
            tr.addView(editText);
        }
        tableLayout.addView(tr, layoutTableRowParams2);


        TableRow.LayoutParams layoutEditTextParams3 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
        layoutEditTextParams3.setMargins(10, 20, 10, 10);

        EditText editText3 = new EditText(activity);
        editText3.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText3.setLayoutParams(layoutEditTextParams3);

        editText3.setTag("" + 22);
        editText3.setTextSize(25);
        editText3.setHint("E-posta *");
        editText3.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        InputFilter[] filters2 = new InputFilter[2];
        filters2[0] = new InputFilter.LengthFilter(127);
        filters2[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (currentChar == '<' && currentChar == '>'&& currentChar == '&'&& currentChar == '='&& currentChar == '*'&& currentChar == '-') {
                            sourceAsSpannableBuilder.delete(i, i + 1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (currentChar != '<' && currentChar != '>'&& currentChar != '&'&& currentChar != '='&& currentChar != '*'&& currentChar != '-') {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };
        editText3.setFilters(filters2);

        EditText editText4 = new EditText(activity);
        editText4.setInputType(InputType.TYPE_CLASS_PHONE);
        editText4.setLayoutParams(layoutEditTextParams3);
        editText4.setHint("Telefon");
        editText4.setTextSize(25);
        editText4.setTag("" + 23);

        InputFilter[] filters = new InputFilter[2];
        filters[0] = new InputFilter.LengthFilter(63);
        filters[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (currentChar == '<' && currentChar == '>'&& currentChar == '&'&& currentChar == '='&& currentChar == '*'&& currentChar == '-') {
                            sourceAsSpannableBuilder.delete(i, i + 1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (currentChar != '<' && currentChar != '>'&& currentChar != '&'&& currentChar != '='&& currentChar != '*'&& currentChar != '-') {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };
        editText4.setFilters(filters);

        TableRow tr2 = new TableRow(activity);

        tr2.setLayoutParams(layoutTableRowParams2);
        tr2.addView(editText3);
        tr2.addView(editText4);
        tableLayout.addView(tr2);

        TableRow.LayoutParams layoutButtonParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        layoutButtonParams.setMargins(0, 35, 0, 50);

        layoutButtonParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        layoutButtonParams.weight = 1;

        Button sendButton = new Button(activity);
        sendButton.setText("Anketi Tamamladım");
        sendButton.setTextSize(25);
        sendButton.setHeight(100);
        sendButton.setMaxWidth(250);
        sendButton.setWidth(250);
        sendButton.setId(R.id.send_button);

        sendButton.setLayoutParams(layoutButtonParams);
        sendButton.setBackgroundResource(R.drawable.buttonstylesurvey);

        TableRow tr3 = new TableRow(activity);

        tr3.setLayoutParams(layoutTableRowParams2);
        tr3.addView(sendButton);
        tableLayout.addView(tr3);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder userInfo = new StringBuilder();
                StringBuilder surveyAnswers = new StringBuilder();
                StringBuilder surveyQuestions = new StringBuilder();

                EditText name = (EditText) tableLayout.findViewWithTag("20");
                EditText surname = (EditText) tableLayout.findViewWithTag("21");
                EditText email = (EditText) tableLayout.findViewWithTag("22");
                EditText phone = (EditText) tableLayout.findViewWithTag("23");
                if(name.getText().toString().isEmpty() || surname.getText().toString().isEmpty() || email.getText().toString().isEmpty()){
                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                    aBuilder.setTitle("Lütfen Gerekli Alanları Doldurunuz");
                    aBuilder.setMessage("Lütfen yıldız ile işaretlenmiş gerekli alanları doldurup tekrar deneyiniz.").setCancelable(false).setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog alertDialog = aBuilder.create();
                    alertDialog.show();
                }
                else {
                userInfo.append(name.getText()).append("*").append(surname.getText()).append("*").append(email.getText()).append("*").append(phone.getText());

                for (int i = 0; i < onlyQuestions.length; i++) {
                    if (Integer.parseInt(onlyPlaces[i]) < 16)
                        surveyAnswers.append(("*" + ((RatingBar) tableLayout.findViewWithTag(onlyPlaces[i])).getRating()).replace('.', ','));
                    else
                        surveyAnswers.append("*").append(((EditText) tableLayout.findViewWithTag(onlyPlaces[i])).getText().toString().replaceAll("\\-",""));
                    surveyQuestions.append("*").append(onlyQuestions[i]);
                }

                if (surveyAnswers.length() >= 1) {
                    surveyAnswers.deleteCharAt(0);
                }

                if (surveyQuestions.length() >= 1) {
                    surveyQuestions.deleteCharAt(0);
                }

                if (g.commonAsyncTask.client != null) {
                    g.commonAsyncTask.client.sendMessage("komut=anketCevaplari&kullaniciBilgileri=" + userInfo.toString() + "&cevapBilgileri=" + surveyAnswers.toString() + "&soruBilgileri=" + surveyQuestions.toString());
                }
                AlertDialog.Builder surveyMessage = new AlertDialog.Builder(context);
                surveyMessage.setTitle("Anket Tamamlandı");
                surveyMessage.setMessage("Anketimize katıldığınız için teşekkür ederiz. Yeniden bekleriz..").setCancelable(false).setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });
                AlertDialog alertDialog = surveyMessage.create();
                alertDialog.show();
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(25);
                ((TextView) alertDialog.findViewById(android.R.id.message)).setTextSize(25);
            }}
        });
    }

}
