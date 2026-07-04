package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import vn.humg.hai.event_ticket_booking_app.R;

public class HelpCenterActivity extends AppCompatActivity {

    private EditText edtSearchFaq;
    private MaterialCardView cardFaq1, cardFaq2, cardFaq3;
    private LinearLayout layoutFaqHeader1, layoutFaqHeader2, layoutFaqHeader3;
    private TextView tvFaqQuestion1, tvFaqQuestion2, tvFaqQuestion3;
    private TextView tvFaqAnswer1, tvFaqAnswer2, tvFaqAnswer3;
    private ImageView ivFaqArrow1, ivFaqArrow2, ivFaqArrow3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        initViews();
        setupFaqToggle();
        setupSearchFilter();
    }

    private void initViews() {
        findViewById(R.id.btn_back_custom).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        edtSearchFaq = findViewById(R.id.edt_search_faq);

        cardFaq1 = findViewById(R.id.card_faq_1);
        cardFaq2 = findViewById(R.id.card_faq_2);
        cardFaq3 = findViewById(R.id.card_faq_3);

        layoutFaqHeader1 = findViewById(R.id.layout_faq_header_1);
        layoutFaqHeader2 = findViewById(R.id.layout_faq_header_2);
        layoutFaqHeader3 = findViewById(R.id.layout_faq_header_3);

        tvFaqQuestion1 = findViewById(R.id.tv_faq_question_1);
        tvFaqQuestion2 = findViewById(R.id.tv_faq_question_2);
        tvFaqQuestion3 = findViewById(R.id.tv_faq_question_3);

        tvFaqAnswer1 = findViewById(R.id.tv_faq_answer_1);
        tvFaqAnswer2 = findViewById(R.id.tv_faq_answer_2);
        tvFaqAnswer3 = findViewById(R.id.tv_faq_answer_3);

        ivFaqArrow1 = findViewById(R.id.iv_faq_arrow_1);
        ivFaqArrow2 = findViewById(R.id.iv_faq_arrow_2);
        ivFaqArrow3 = findViewById(R.id.iv_faq_arrow_3);
    }

    private void setupFaqToggle() {
        layoutFaqHeader1.setOnClickListener(v -> toggleFaq(tvFaqAnswer1, ivFaqArrow1));
        layoutFaqHeader2.setOnClickListener(v -> toggleFaq(tvFaqAnswer2, ivFaqArrow2));
        layoutFaqHeader3.setOnClickListener(v -> toggleFaq(tvFaqAnswer3, ivFaqArrow3));
    }

    private void toggleFaq(TextView answerView, ImageView arrowView) {
        if (answerView.getVisibility() == View.GONE) {
            answerView.setVisibility(View.VISIBLE);
            arrowView.setImageResource(android.R.drawable.arrow_up_float);
        } else {
            answerView.setVisibility(View.GONE);
            arrowView.setImageResource(android.R.drawable.arrow_down_float);
        }
    }

    private void setupSearchFilter() {
        edtSearchFaq.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFaqs(s.toString().trim().toLowerCase());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFaqs(String query) {
        if (query.isEmpty()) {
            cardFaq1.setVisibility(View.VISIBLE);
            cardFaq2.setVisibility(View.VISIBLE);
            cardFaq3.setVisibility(View.VISIBLE);
            return;
        }

        // Lọc theo câu hỏi 1
        String q1 = tvFaqQuestion1.getText().toString().toLowerCase();
        String a1 = tvFaqAnswer1.getText().toString().toLowerCase();
        if (q1.contains(query) || a1.contains(query)) {
            cardFaq1.setVisibility(View.VISIBLE);
        } else {
            cardFaq1.setVisibility(View.GONE);
        }

        // Lọc theo câu hỏi 2
        String q2 = tvFaqQuestion2.getText().toString().toLowerCase();
        String a2 = tvFaqAnswer2.getText().toString().toLowerCase();
        if (q2.contains(query) || a2.contains(query)) {
            cardFaq2.setVisibility(View.VISIBLE);
        } else {
            cardFaq2.setVisibility(View.GONE);
        }

        // Lọc theo câu hỏi 3
        String q3 = tvFaqQuestion3.getText().toString().toLowerCase();
        String a3 = tvFaqAnswer3.getText().toString().toLowerCase();
        if (q3.contains(query) || a3.contains(query)) {
            cardFaq3.setVisibility(View.VISIBLE);
        } else {
            cardFaq3.setVisibility(View.GONE);
        }
    }
}
