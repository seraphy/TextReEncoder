package jp.seraphyware.textencodechanger.services;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * バックグラウンドタスク用サービス.
 *
 * @author seraphy
 */
@Component
public class BackgroundTaskService {

    /**
     * ロガー.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * スレッドサービス.
     */
    private ExecutorService executor;

    /**
     * 初期化.
     */
    @PostConstruct
    public final void init() {
        log.info("★FileWalkService#init");
        executor = Executors.newFixedThreadPool(1);
    }

    /**
     * 破棄処理.
     */
    @PreDestroy
    public final void dispose() {
        log.info("★FileWalkService#dispose");
        executor.shutdownNow();
    }

    /**
     * バックグラウンドジョブのキューに入れFutureを返す.
     *
     * @param <V> データ型
     * @param task タスク
     * @return Future
     */
    public final <V> Future<V> execute(final Callable<V> task) {
        Objects.requireNonNull(task);
        return executor.submit(task);
    }

    /**
     * バックグラウンドジョブのキューに入れる.
     *
     * @param task タスク
     */
    public final void execute(final Runnable task) {
        Objects.requireNonNull(task);
        executor.execute(task);
    }
}
