package pl.edu.pjwstk.kaldi.service.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

public class ParallelTask extends Task {

    private final static Logger logger = LoggerFactory.getLogger(ParallelTask.class);

    private List<Task> tasks = new LinkedList<>();

    @Override
    public void run() {

        state = State.RUNNING;

        logger.info("Parallel task starting tasks in parallel...");
        List<Thread> threads = new LinkedList<>();
        for (Task task : tasks) {
            Thread th = new Thread(task);
            th.start();
            threads.add(th);
        }

        logger.info("Parallel task waiting for tasks to finish...");

        for (Thread th : threads) {
            try {
                th.join();
            } catch (InterruptedException e) {
                // TODO: wait until actually finished?
            }
        }

        logger.info("All parallel tasks finished!");

        for (Task task : tasks) {
            if (task.state != State.SUCCEEDED) {
                logger.info("A parallel task was not succesfull!");
                state = State.FAILED;
                return;
            }
        }

        state = State.SUCCEEDED;
        logger.info("Parallel task succesful!");
    }

    @Override
    public void loadSettings(XPath xpath, Element node)
            throws XPathExpressionException {

        NodeList tasks = (NodeList) xpath.evaluate("task", node,
                XPathConstants.NODESET);

        for (int i = 0; i < tasks.getLength(); i++) {
            Element elTask = (Element) tasks.item(i);

            String name = elTask.getAttribute("name");

            Task task = getTask(name);

            task.loadSettings(xpath, elTask);
        }
    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {
        for (Task task : tasks) {

            //TODO: also check names of tasks!
            task.updateHash(m);
        }

    }
}
