package com.nasnav.service.scheduler;

import com.nasnav.persistence.SchedulerTaskEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.DomainService;
import com.nasnav.service.MailService;
import com.nasnav.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static com.nasnav.constatnts.EmailConstants.*;

@Service
public class ScheduleTaskHelper {
    TaskScheduler scheduler;
    Map<Long, ScheduledFuture<?>> jobsMap = new HashMap<>();
    @Autowired
    private MailService mailService;
    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private DomainService domainService;
    @Autowired
    private  OrganizationService orgService;

    private ScheduledExecutorService scheduledExecutorService;
    public ScheduleTaskHelper(TaskScheduler scheduler) {

        this.scheduler = scheduler;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }
    public void sendMailAndAddTaskToScheduler(SchedulerTaskEntity schedulerTaskEntity) {
        if (LocalDateTime.now().isBefore(schedulerTaskEntity.getStartsAt())) {
            addTaskToScheduler(schedulerTaskEntity);
            try {
                sendAppointmentEmails(schedulerTaskEntity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void addTaskToScheduler(SchedulerTaskEntity schedulerTaskEntity) {
        if (LocalDateTime.now().isBefore(schedulerTaskEntity.getStartsAt())) {
            long initialDelay = LocalDateTime.now()
                    .until(schedulerTaskEntity.getStartsAt(), ChronoUnit.MILLIS);
            System.out.println(initialDelay);
            ScheduledFuture<?> scheduledTask = scheduledExecutorService.schedule(() -> {
                try {
                    if ("appointment".equals(schedulerTaskEntity.getType())) {
                        sendAppointmentEmails(schedulerTaskEntity);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, initialDelay, TimeUnit.MILLISECONDS);

            jobsMap.put(schedulerTaskEntity.getId(), scheduledTask);
        }
    }

    private void sendAppointmentEmails(SchedulerTaskEntity schedulerTaskEntity) throws MessagingException, IOException {
        String orgName = schedulerTaskEntity.getAvailability().getOrganization().getName();
        String userEmail = schedulerTaskEntity.getAvailability().getUser().getEmail();
        String employeeEmail = schedulerTaskEntity.getAvailability().getEmployeeUser().getEmail();
        Map<String, String> parametersMap = createAppointmentEmailParameters(schedulerTaskEntity);

        mailService.send(orgName, userEmail, "Appointment", NEW_CLIENT_EMAIL_Booked_Appointment_TEMPLATE,parametersMap);
        mailService.send(orgName, employeeEmail, "Appointment",NEW_EMPLOYEE_EMAIL_Booked_Appointment_TEMPLATE,parametersMap);
    }

    public void removeTaskFromScheduler(SchedulerTaskEntity schedulerTaskEntity) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(schedulerTaskEntity.getId());
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            jobsMap.remove(schedulerTaskEntity.getId());
        }
    }

    private Map<String, String> createAppointmentEmailParameters(SchedulerTaskEntity schedulerTaskEntity) {
        String domain = domainService.getBackendUrl();
        UserEntity user= schedulerTaskEntity.getAvailability().getUser();
        String orgDomain = domainService.getOrganizationDomainAndSubDir(user.getOrganizationId());

        String orgLogo = domain + "/files/"+ orgService.getOrgLogo(user.getOrganizationId());

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(USERNAME_PARAMETER,user.getName());
        parametersMap.put("orgDomain", orgDomain);
        parametersMap.put("orgLogo", orgLogo);
        parametersMap.put("#ClientName#", user.getName());
        parametersMap.put("##EmployeeName##", schedulerTaskEntity.getEmployeeUser().getName());
        parametersMap.put("#AppointmentProvider#", schedulerTaskEntity.getAvailability().getOrganization().getName());
        parametersMap.put("#AppointmentDate#", String.valueOf(schedulerTaskEntity.getAvailability().getStartsAt().toLocalDate()));
        parametersMap.put("#AppointmentTime#", String.valueOf(schedulerTaskEntity.getAvailability().getStartsAt().toLocalTime()));

        parametersMap.put("year",String.valueOf(LocalDateTime.now().getYear()));
        return parametersMap;
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



