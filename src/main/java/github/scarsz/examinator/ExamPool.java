package github.scarsz.examinator;

import github.scarsz.examinator.exam.ExamCallable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ExamPool {

    private List<ExamCallable> activeExams = new ArrayList<>();
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    ExamPool(Examinator examinator) {
        examinator.getJda().addEventListener(new ListenerAdapter() {
            @Override
            public void onStatusChange(StatusChangeEvent event) {
                if (event.getStatus() == JDA.Status.SHUTTING_DOWN) {
                    System.out.println("Cleaning up " + activeExams.size() + " active exams");
                    new ArrayList<>(activeExams).forEach(ExamCallable::destroy);
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> threadPool.shutdown()));
    }

    public void timedCall(Callable<Object> callable) {
        FutureTask task = new FutureTask<>(callable);
        threadPool.execute(task);
    }

    public <T> T timedCallBlocking(Callable<T> callable, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<T> task = new FutureTask<>(callable);
        threadPool.execute(task);
        return task.get(timeout, timeUnit);
    }

    public List<ExamCallable> getActiveExams() {
        return activeExams;
    }

}
