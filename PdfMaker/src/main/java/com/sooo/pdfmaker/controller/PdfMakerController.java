package com.sooo.pdfmaker.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.sooo.pdfmaker.domain.TagStrVO;
import com.sooo.pdfmaker.service.PdfMakerService;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RequestMapping("/*")
@Controller
@Log4j
public class PdfMakerController {
	
	@Setter(onMethod_ = {@Autowired})
	PdfMakerService service;
	
	@GetMapping(value="/")
	public String getIndex() {
		return "index";
	}

	@GetMapping(value="/getTagStr.do", produces = "application/text; charset=utf8")
	public @ResponseBody String getTagStr() throws UnsupportedEncodingException {
		
		List<TagStrVO> tagStrList = service.getTagStr();
		List<TagStrVO> tagList = service.getTagList();
		List<TagStrVO> settingList = service.getSettingList();
		
		HashMap<String, List> map = new HashMap<>();
		map.put("tagStr", tagStrList);
		map.put("tag", tagList);
		map.put("setting", settingList);
		
		Gson gson = new Gson();
		String json = gson.toJson(map);
		
		return json;
	}
	
}