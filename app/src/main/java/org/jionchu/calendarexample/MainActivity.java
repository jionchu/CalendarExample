package org.jionchu.calendarexample;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.WeekFields;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarView mCalendarView;
    private CalendarDay mSelectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidThreeTen.init(this);

        // 앱 시작 시 달력에 오늘 날짜 표시
        final CalendarDay defaultSelected = new CalendarDay(LocalDate.now(),DayOwner.THIS_MONTH);
        final YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(10);
        YearMonth endMonth = currentMonth.plusMonths(10);
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        mSelectedDay = defaultSelected; // selected day 오늘 날짜로 초기화

        /* find view by id */
        mCalendarView = findViewById(R.id.main_calendar_view);
        TextView tvMonth = findViewById(R.id.main_tv_month);

        /* calendar header initialize */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String currentDate = sdf.format(new Date());
        tvMonth.setText(currentDate);

        /* calendar view setting */
        DayBinder<DayViewContainer> dayBinder = new DayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer dayViewContainer, CalendarDay calendarDay) {
                dayViewContainer.tvDay.setText(String.valueOf(calendarDay.getDay()));
                dayViewContainer.calendarDay = calendarDay;

                if (calendarDay.equals(mSelectedDay)) { // default selected day의 경우 글씨 색상 초기화
                    dayViewContainer.tvDay.setTextColor(getResources().getColor(R.color.colorPrimary));
                    dayViewContainer.tvDay.setTypeface(Typeface.DEFAULT_BOLD);
                }
                else { // 이전에 select 되었던 날짜가 더 이상 select 상태가 아닐 때 글씨 색상 변경
                    dayViewContainer.tvDay.setTextColor(getResources().getColor(R.color.colorText));
                    dayViewContainer.tvDay.setTypeface(Typeface.DEFAULT);
                }

                // 현재 달에 해당하는 날짜들만 보이게 설정
                if (calendarDay.getOwner() == DayOwner.THIS_MONTH) {
                    dayViewContainer.llCalendar.setVisibility(View.VISIBLE);
                } else {
                    dayViewContainer.llCalendar.setVisibility(View.GONE);
                }

            }
        };

        mCalendarView.setDayBinder(dayBinder);
        mCalendarView.setup(startMonth, endMonth, firstDayOfWeek);
        mCalendarView.scrollToMonth(currentMonth);

    }

    class DayViewContainer extends ViewContainer {

        private CalendarDay calendarDay;
        private ImageView ivSchedule;
        private TextView tvDay, tvState;
        private LinearLayout llCalendar;

        DayViewContainer(View view) {
            super(view);
            tvDay = view.findViewById(R.id.calendar_tv_day);
            tvState = view.findViewById(R.id.calendar_tv_state);
            ivSchedule = view.findViewById(R.id.calendar_iv_schedule);
            llCalendar = view.findViewById(R.id.calendar_ll);

            // 해당 날짜 선택되었을 때
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 직전에 선택되었던 날짜가 아닌 경우
                    if (calendarDay != mSelectedDay) {

                        // 이전에 선택되었던 날짜 다시 검은색으로 변경 (bind 메서드 실행)
                        mCalendarView.notifyDayChanged(mSelectedDay);

                        // selected day update
                        mSelectedDay = calendarDay;
                    }

                    tvDay.setTextColor(getResources().getColor(R.color.colorPrimary)); // 색상 변경
                    tvDay.setTypeface(Typeface.DEFAULT_BOLD); // bold체로 변경
                }
            });
        }
    }

}
