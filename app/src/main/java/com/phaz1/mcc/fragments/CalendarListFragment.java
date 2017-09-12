package com.phaz1.mcc.fragments;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.orm.query.Condition;
import com.orm.query.Select;

import com.phaz1.mcc.R;
import com.phaz1.mcc.activities.TabActivity;
import com.phaz1.mcc.adapters.CallScheduleAdapter;
import com.phaz1.mcc.datalayer.CallTemplate;

import java.util.Iterator;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarListFragment extends Fragment {

    private ListView listView;

    public CalendarListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_calendar_list, container, false);
        listView = (ListView) view.findViewById(R.id.listSchedule);
        SegmentedGroup group = (SegmentedGroup) view.findViewById(R.id.segGroup);
        loadListView(0);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                loadListView(Integer.parseInt(view.findViewById(i).getTag().toString()));
            }
        });
        return view;
    }

    private void loadListView(int complete) {
        List<CallTemplate> tmp = Select.from(CallTemplate.class)
                .where(Condition.prop("complete").eq(complete))
                .list();
        int a = 1;
        if (a == 0) {
            Iterator<CallTemplate> iter = tmp.iterator();
            while (iter.hasNext()) {
                CallTemplate ct = iter.next();
//            String description = "Forget About It : " + ct.getId();
                CalendarProvider provider = new CalendarProvider(getActivity());
                List<Calendar> calendars = provider.getCalendars().getList();
                boolean stillExistsInOneOfCalendar = false;
                for (Calendar c : calendars) {
                    try {
                        List<Event> events = provider.getEvents(c.id).getList();
                        try {
                            for (Event ev : events) {
                                Log.d("FAI", c.accountName + "::" + c.accountType + ":" + ev.description + ":" + ev.displayName + " : " /*+ description*/);
                                if (ev.description.contains(getString(R.string.app_name)) && ev.description.contains(ct.getTitle())) {
                                    stillExistsInOneOfCalendar = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {

                        }
                    } catch (Exception e) {

                    }
                }
                if (!stillExistsInOneOfCalendar)
                    iter.remove();
            }
        }
        CallScheduleAdapter adapter = new CallScheduleAdapter((TabActivity) this.getActivity(), tmp, complete);
        listView.setAdapter(adapter);
    }
}