package com.sooo.pdfmaker.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sooo.pdfmaker.service.PdfDownloadServiceImpl;

import lombok.Setter;

@RequestMapping("/*")
@Controller
public class PdfDownloadController{

	@Setter(onMethod_ = { @Autowired })
	PdfDownloadServiceImpl service;

	@PostMapping(value = "/downloadFromMaker.do", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public void downloadFromMaker(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String pdfBody) throws Exception {

		String pdfStr = URLDecoder.decode(pdfBody.replace("pdfBody=", ""), "UTF-8");
		String pdfPath = request.getServletContext().getRealPath("/resources/tmp");
		String str = service.pdfDownload(pdfStr, pdfPath);

		File file = new File(str);
		FileInputStream fis = null;
		OutputStream out = null;

		try {
			
			String reFilename = "pdfMaker.pdf";
			response.setHeader("Content-Type", "application/octet-stream;charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + reFilename + "\";");
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Length", (int) file.length() + "");
			
			out = response.getOutputStream();
			fis = new FileInputStream(file);
			FileCopyUtils.copy(fis, out);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				out.flush();
				out.close();
				file.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
