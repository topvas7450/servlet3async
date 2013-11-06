package com.kai.spring.async;

import java.io.Writer;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/asyncdemo")
public class AsyncDemoController {
	Logger logger = Logger.getLogger(AsyncDemoController.class);
	
	private final Queue<DeferredResult<ModelAndView>> eventQueue = new ConcurrentLinkedQueue<DeferredResult<ModelAndView>>();
	
    @RequestMapping("/normal")
    public String normalCall(Model model) throws InterruptedException {
		long start = System.currentTimeMillis();
		Thread.sleep(2000);
		String name = Thread.currentThread().getName();
		long duration = System.currentTimeMillis() - start;
		String msg = String.format(Locale.getDefault(), "Thread %s completed the task in %d ms.", name, duration);
		model.addAttribute("result", msg);
		
		return "result";
    }

    @RequestMapping("/async")
    public Callable<String> asyncCall(final Model model) {
    	
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
            	long start = System.currentTimeMillis();
        		Thread.sleep(2000);
        		String name = Thread.currentThread().getName();
        		long duration = System.currentTimeMillis() - start;
        		String msg = String.format(Locale.getDefault(), "Thread %s completed the task in %d ms.", name, duration);
        		model.addAttribute("result", msg);
        		
        		return "result";
            }
        };
    }
 
    @RequestMapping("/deferred")
    public DeferredResult<ModelAndView> deferredCall() {
        DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();
        this.eventQueue.add(result);
        return result;
    }
 
    @Scheduled(fixedRate = 5000)
    public void simulateExternalThread() throws InterruptedException {
    	logger.debug("simulateExternalThread...");
        Thread.sleep(2000);
        for (DeferredResult<ModelAndView> result : this.eventQueue) {
        	ModelAndView model = new ModelAndView("result");
        	long start = System.currentTimeMillis();
    		String name = Thread.currentThread().getName();
    		long duration = System.currentTimeMillis() - start;
    		String msg = String.format(Locale.getDefault(), "Thread %s completed the task in %d ms.", name, duration);
    		model.addObject("result", msg);
    		
            result.setResult(model);
            this.eventQueue.remove(result);
        }
    }
}
