// package com.clinomics.controller;

// import java.io.IOException;
// import java.util.Map;

// import javax.servlet.ServletOutputStream;
// import javax.servlet.http.HttpServletResponse;

// import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.multipart.MultipartHttpServletRequest;

// import com.clinomics.service.BundleService;
// import com.clinomics.service.SampleExcelService;
// import com.clinomics.service.SampleItemService;
// import com.clinomics.service.SampleService;
// import com.clinomics.service.SampleTempService;

// @RequestMapping("/work/register")
// @Controller
// public class RegisterController {

// 	@Autowired
// 	SampleService sampleService;

// 	@Autowired
// 	SampleTempService sampleTempService;
	
// 	@Autowired
// 	SampleItemService sampleItemService;

// 	@Autowired
// 	SampleExcelService sampleExcelService;

// 	@Autowired
// 	BundleService bundleService;
	
// 	@GetMapping()
// 	public String add(Model model) {
// 		model.addAttribute("bundles", bundleService.selectAll());
		
// 		return "work/register";
// 	}
	
// 	@GetMapping("/get")
// 	@ResponseBody
// 	public Map<String, Object> get(@RequestParam Map<String, String> params) {
// 		return sampleService.findSampleByBundleAndDateResultNullStatusFail(params);
// 	}
	
// 	@GetMapping("/get/temp")
// 	@ResponseBody
// 	public Map<String, Object> getTemp(@RequestParam Map<String, String> params) {
// 		return sampleTempService.findAll(params);
// 	}
	
// 	@PostMapping("/save")
// 	@ResponseBody
// 	public Map<String, String> save(@RequestBody Map<String, String> datas) {
		
// 		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
// 		datas.put("memberId", userDetails.getUsername());
		
// 		return sampleService.save(datas);
// 	}
	
// 	@GetMapping("/import")
// 	@ResponseBody
// 	public Map<String, Object> importExcelToTable() {
		
// 		return sampleTempService.importExcelToSample();
// 	}
	
// 	@GetMapping("/itemby/sample/{id}")
// 	@ResponseBody
// 	public Map<String, Object> getItemBySample(@PathVariable String id) {
// 		return sampleItemService.findSampleItemBySample(id);
// 	}
	
// 	@GetMapping("/itemby/bundle/{id}")
// 	@ResponseBody
// 	public Map<String, Object> getItemByBundle(@PathVariable String id) {
// 		return sampleItemService.findSampleItemByBundle(id);
// 	}
	
// 	@PostMapping("/excel/import")
// 	@ResponseBody
// 	public Map<String, Object> importExcelSample(@RequestParam("file") MultipartFile multipartFile, MultipartHttpServletRequest request)
// 			throws InvalidFormatException, IOException {
		
// 		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
// 		String memberId = userDetails.getUsername();
		
// 		String bundleId = request.getParameter("bundleId").toString();
		
// 		return sampleExcelService.importExcelSample(multipartFile, bundleId, memberId);
// 	}
	
// 	@GetMapping("/excel/form")
// 	@ResponseBody
// 	public void exportExcelForm(@RequestParam Map<String, String> params, HttpServletResponse response) {
// 		XSSFWorkbook xlsx = sampleExcelService.exportExcelForm(params);
// 		requestExcel(xlsx, response);
// 	}
	
// 	private void requestExcel(XSSFWorkbook xlsx, HttpServletResponse response) {
// 		response.setHeader("Content-Disposition", "attachment;filename=sample.xlsx");
//         // 엑셀파일명 한글깨짐 조치
//         response.setHeader("Content-Transfer-Encoding", "binary;");
//         response.setHeader("Pragma", "no-cache;");
//         response.setHeader("Expires", "-1;");
//         response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

//         try {
// 	        ServletOutputStream out = response.getOutputStream();
	        
//         	xlsx.write(out);
//         	xlsx.close();
// 			out.flush();
// 		} catch (IOException e) {
// 			// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
// 	}
// }
