package com.kai.spring.async;

import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/asyncdemo")
public class AsyncDemoController {
	Logger logger = Logger.getLogger(AsyncDemoController.class);
	
	private final Queue<DeferredResult<ModelAndView>> eventQueue = new ConcurrentLinkedQueue<DeferredResult<ModelAndView>>();
	private final Map<String, Map<String, Object>> watchers = new ConcurrentHashMap<String, Map<String, Object>>();

	
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
    public DeferredResult<ModelAndView> deferredCall(HttpSession session) throws InterruptedException {
    	Map<String, Object> job = new HashMap<String, Object>();
        DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>(10000L, "result");
//        this.eventQueue.add(result);
        job.put("deferredResult", result);
     // this is simulate every request work cost time
        job.put("workCostTime", Integer.valueOf((int)(Math.random()*10)));
        String name = Thread.currentThread().getName();
        job.put("requestThreadName", name);
    	this.watchers.put(session.getId(), job);
//    	String msg = String.format(Locale.getDefault(), "Session id %s ,Thread %s get request.", session.getId(), name);
//		logger.debug(msg);
		
        return result;
    }
 
    @Scheduled(fixedRate = 2000)
    public void simulateExternalThread() throws InterruptedException {
//    	logger.debug("----------------------...before work:"+new Date());
//        Thread.sleep(10000);
//        logger.debug("----------------------...after work:"+new Date());
//        for (DeferredResult<ModelAndView> result : this.eventQueue) {
//        	ModelAndView model = new ModelAndView("result");
//        	long start = System.currentTimeMillis();
//    		String name = Thread.currentThread().getName();
//    		long duration = System.currentTimeMillis() - start;
//    		String msg = String.format(Locale.getDefault(), "Thread %s completed the task in %d ms.", name, duration);
//    		model.addObject("result", msg);
//    		
//            result.setResult(model);
//            this.eventQueue.remove(result);
//        }
        
        for (Map.Entry<String, Map<String, Object>> entry : watchers.entrySet()) {
        	String sessionId = entry.getKey();
        	Map<String, Object> map = entry.getValue();
        	
        	DeferredResult<ModelAndView> deferredResult = (DeferredResult<ModelAndView>) map.get("deferredResult");
        	// this is simulate every request work cost time, but this is only one thread in this method
        	// how to separate the work
        	Integer workCostTime = (Integer) map.get("workCostTime"); 
        	
        	String requestThreadName = (String) map.get("requestThreadName");
        	
        	ModelAndView model = new ModelAndView("result");
    		String name = Thread.currentThread().getName();
    		String msg = String.format(Locale.getDefault(), "Session id %s , request Thread %s, work Thread %s, need work cost time %d sec.", sessionId, requestThreadName, name, workCostTime);
    		logger.debug(msg);
    		model.addObject("result", msg);
    		
    		deferredResult.setResult(model);
    		
    		this.watchers.remove(sessionId);
        }
    }
    
    @ExceptionHandler
	@ResponseBody
	public String handleException(IllegalStateException ex) {
		return "Handled exception: " + ex.getMessage();
	}
}
