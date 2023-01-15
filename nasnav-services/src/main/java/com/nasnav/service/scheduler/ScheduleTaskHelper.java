package com.nasnav.service.scheduler;

import com.nasnav.persistence.SchedulerTaskEntity;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduleTaskHelper {
    TaskScheduler scheduler;
    Map<Long, ScheduledFuture<?>> jobsMap = new HashMap<>();

    public ScheduleTaskHelper(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void addTaskToScheduler(SchedulerTaskEntity schedulerTaskEntity) {
        if(LocalDateTime.now().isBefore(schedulerTaskEntity.getStartsAt())){
            ScheduledFuture<?> scheduledTask = scheduler.schedule(new Runnable() {
                  @Override
                  public void run() {
                      try {
                          // we can run any task
                          System.out.println("Running Task Schedular  NUM ...: " + schedulerTaskEntity.getId() + "  " + schedulerTaskEntity.getStartsAt() + "   " + Calendar.getInstance().getTime());

                      } catch (Exception e) {
                          throw new RuntimeException(e);
                      }
                  }
            }
            , Date.from(schedulerTaskEntity.getStartsAt().atZone(ZoneId.systemDefault()).toInstant())
            );
            jobsMap.put(schedulerTaskEntity.getId(), scheduledTask);
        }
    }

    public void removeTaskFromScheduler(SchedulerTaskEntity schedulerTaskEntity) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(schedulerTaskEntity.getId());
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            jobsMap.remove(schedulerTaskEntity.getId());
        }
    }

    @EventListener({ContextRefreshedEvent.class})
    void contextRefreshedEvent() {
        // Get all tasks from DB and reschedule them in case of context restarted
    }
}
