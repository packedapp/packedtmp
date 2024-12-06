==== A job is typically adds a bit more overhead to the task, than simply using a ExecutorService ====
* A job has a unique name within a namespace
* A job can have a context injected
* The status of a job can be tracked in JobTracker
* A job can be tagged 



----------- Properties
      [Daemon | Non-Daemon]  (Threads)
      [Daemon | Non-Daemon]  (Background)
      [Scheduled | Non-Scheduled]
        [Recurrent | Once]
        [Duration | Calendar]
      [Tags]
----------- Types
Job
  -> NonScheduled
  -> Scheduled
      
      
     -> Once
     -> Duration WithFixedDelay/Rate
     -> Calender Cron
     
     ScheduledJobTriggerKind DURATION|CALENDAR