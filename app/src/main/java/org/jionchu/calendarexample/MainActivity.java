package org.jionchu.calendarexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarView mCalendarView;
    private CalendarDay mSelectedDay;
    private ArrayList<Schedule> mDayScheduleList;
    private HashMap<LocalDate,ArrayList<Schedule>> mMonthScheduleList;

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
        mDayScheduleList = new ArrayList<>();
        mMonthScheduleList = new HashMap<>();

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
                    // 구글 캘린더에 일정이 있는 경우 표시
                    if (mMonthScheduleList.get(calendarDay.getDate())!=null) {
                        dayViewContainer.ivSchedule.setVisibility(View.VISIBLE);
                        if (mMonthScheduleList.get(calendarDay.getDate()).size()>2) {
                            dayViewContainer.tvState.setText("바쁨");
                            dayViewContainer.tvState.setTextColor(getResources().getColor(R.color.colorBusy));
                        } else {
                            dayViewContainer.tvState.setText("여유");
                            dayViewContainer.tvState.setTextColor(getResources().getColor(R.color.colorComfort));
                        }
                    }
                    else {
                        dayViewContainer.ivSchedule.setVisibility(View.INVISIBLE);
                        dayViewContainer.tvState.setText("");
                    }
                } else {
                    dayViewContainer.llCalendar.setVisibility(View.GONE);
                }

            }
        };

        mCalendarView.setDayBinder(dayBinder);
        mCalendarView.setup(startMonth, endMonth, firstDayOfWeek);
        mCalendarView.scrollToMonth(currentMonth);

        // 이번 달 구글 캘린더 일정 가져오기
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            readEvents(currentMonth.getYear(), currentMonth.getMonthValue());
            for (LocalDate keyDate : mMonthScheduleList.keySet())
                mCalendarView.notifyDateChanged(keyDate);
        } else { // 권한 없는 경우 권한 요청하기
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1001);
        }

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

    void readEvents(int year, int month) {
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.ORGANIZER
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_BEGIN_INDEX = 1;
        final int PROJECTION_TITLE_INDEX = 2;
        final int PROJECTION_ORGANIZER_INDEX = 3;

        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(year, month-1, 1, 8, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month-1, 31, 8, 0);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        Cursor cur =  this.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, null, null, null);

        mMonthScheduleList.clear();

        while (cur.moveToNext()) {

            // Get the field values
            long eventID = cur.getLong(PROJECTION_ID_INDEX);
            long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            String title = cur.getString(PROJECTION_TITLE_INDEX);
            String organizer = cur.getString(PROJECTION_ORGANIZER_INDEX);

            // Do something with the values.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");

            LocalDate date = LocalDate.of(calendar.getTime().getYear()+1900,calendar.getTime().getMonth()+1,calendar.getTime().getDate());
            ArrayList<Schedule> schedules = mMonthScheduleList.get(date);
            if (schedules == null)
                schedules = new ArrayList<>();
            schedules.add(new Schedule(title,timeFormat.format(calendar.getTime())));
            Collections.sort(schedules);
            mMonthScheduleList.put(date,schedules);

        }

    }

}
