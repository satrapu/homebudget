package org.testng.internal.thread.graph;

import org.testng.collections.Lists;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches. It takes a {@code DynamicGraph}
 * of tasks to be run and a {@code IThreadWorkerFactory} to initialize/create
 * {@code Runnable} wrappers around those tasks
 */
public class GraphThreadPoolExecutor<T> extends ThreadPoolExecutor {
  private static final boolean DEBUG = false;
  /** Set to true if you want to generate GraphViz graphs */
  private static final boolean DOT_FILES = false;

  private DynamicGraph<T> m_graph;
  private List<Runnable> m_activeRunnables = Lists.newArrayList();
  private IThreadWorkerFactory<T> m_factory;
  private List<String> m_dotFiles = Lists.newArrayList();

  public GraphThreadPoolExecutor(DynamicGraph<T> graph, IThreadWorkerFactory<T> factory, int corePoolSize,
      int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    ppp("Initializing executor with " + corePoolSize + " threads and following graph " + graph);
    m_graph = graph;
    m_factory = factory;
  }

  public void run() {
    synchronized(m_graph) {
      if (DOT_FILES) {
        m_dotFiles.add(m_graph.toDot());
      }
      Set<T> freeNodes = m_graph.getFreeNodes();
      runNodes(freeNodes);
    }
  }

  /**
   * Create one worker per node and execute them.
   */
  private void runNodes(Set<T> nodes) {
    List<IWorker<T>> runnables = m_factory.createWorkers(nodes);
    for (IWorker<T> r : runnables) {
      m_activeRunnables.add(r);
      ppp("Added to active runnable");
      setStatus(r, Status.RUNNING);
      ppp("Executing: " + r);
      try {
        execute(r);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setStatus(IWorker<T> worker, Status status) {
    ppp("Set status:" + worker + " status:" + status);
    synchronized(m_graph) {
      for (T m : worker.getTasks()) {
        m_graph.setStatus(m, status);
      }
    }
  }

  @Override
  public void afterExecute(Runnable r, Throwable t) {
    ppp("Finished runnable:" + r);
    m_activeRunnables.remove(r);
    setStatus((IWorker<T>) r, Status.FINISHED);
    synchronized(m_graph) {
      ppp("Node count:" + m_graph.getNodeCount() + " and "
          + m_graph.getNodeCountWithStatus(Status.FINISHED) + " finished");
      if (m_graph.getNodeCount() == m_graph.getNodeCountWithStatus(Status.FINISHED)) {
        ppp("Shutting down executor " + this);
        if (DOT_FILES) {
          generateFiles(m_dotFiles);
        }
        shutdown();
      } else {
        if (DOT_FILES) {
          m_dotFiles.add(m_graph.toDot());
        }
        Set<T> freeNodes = m_graph.getFreeNodes();
        runNodes(freeNodes);
      }
    }
//    if (m_activeRunnables.isEmpty() && m_index < m_runnables.getSize()) {
//      runNodes(m_index++);
//    }
  }

  private void generateFiles(List<String> files) {
    try {
      File dir = File.createTempFile("TestNG-", "");
      dir.delete();
      dir.mkdir();
      for (int i = 0; i < files.size(); i++) {
        File f = new File(dir, "" + (i < 10 ? "0" : "") + i + ".dot");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.append(files.get(i));
        bw.close();
      }
      if (DOT_FILES) {
        System.out.println("Created graph files in " + dir);
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  private void ppp(String string) {
    if (DEBUG) {
      System.out.println("   [GraphThreadPoolExecutor] " + Thread.currentThread().getId() + " "
          + string);
    }
  }

}
