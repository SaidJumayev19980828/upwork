package com.nasnav.service.scheduler;

import com.nasnav.persistence.SchedulerTaskEntity;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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
    @Autowired
    private MailService mailService;
    @Autowired
    private BankAccountService bankAccountService;

    public ScheduleTaskHelper(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void addTaskToScheduler(SchedulerTaskEntity schedulerTaskEntity) {
        if(LocalDateTime.now().isBefore(schedulerTaskEntity.getStartsAt())){
            ScheduledFuture<?> scheduledTask = scheduler.schedule(new Runnable() {
                  @Override
                  public void run() {
                      try {
                          switch (schedulerTaskEntity.getType()){
                              case "appointment":{
                                    mailService.send(schedulerTaskEntity.getAvailability().getOrganization().getName(),schedulerTaskEntity.getAvailability().getUser().getEmail(),
                                  "Appointment",null,null);
                                    mailService.send(schedulerTaskEntity.getAvailability().getOrganization().getName(),schedulerTaskEntity.getAvailability().getEmployeeUser().getEmail(),
                                  "Appointment",null,null);
                              }
                          }
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

    @Scheduled(cron = "@monthly")
    @Async
    public void run() {
        bankAccountService.setAllAccountsOpeningBalance();
    }

}
