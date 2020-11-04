package com.sooo.pdfmaker.service;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.sooo.pdfmaker.domain.ContentDto;
import com.sooo.pdfmaker.domain.LineDto;
import com.sooo.pdfmaker.domain.PDFDto;
import com.sooo.pdfmaker.domain.TableDto;
import com.sooo.pdfmaker.domain.TagStrVO;
import com.sooo.pdfmaker.domain.TextDto;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@AllArgsConstructor
public class PdfDownloadServiceImpl implements PdfDownloadService {

	private static List<TagStrVO> settingList;
	private static List<TagStrVO> tagList;
	
	@Autowired
	PdfMakerService service;
	
	public void downloadFromFile(String path, String pdfPath) throws Exception {
		InputStream is = new FileInputStream(path);
		Scanner scan = new Scanner(System.in);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(is));

		String in;
		String pdfBody = "";
		while ((in = buffer.readLine()) != null) {
			in = in.replaceAll("[\\s]*=[\\s]*", "=");
			pdfBody = pdfBody.concat(in.trim());
		}
		pdfBody = pdfBody.trim();
		pdfDownload(pdfBody, pdfPath);
	}
	
	public String pdfDownload(String pdfBody, String pdfPath) throws Exception {
		// 문서 만들기
//		final PDDocument doc = new PDDocument();

		setDefault();

		final PDDocument doc = makeDocument(pdfBody);

		File dir = new File(pdfPath);
		
		if (!dir.exists()) {
			dir.mkdir();
		}

		File file = new File(pdfPath + "/" + doc.hashCode());
		while (file.exists()) {
			file = new File(file.getPath() + "1");
		}
		doc.save(file);
		doc.close();

		return file.getPath();
		
		// 한글폰트
//		InputStream fontStream1 = new FileInputStream("C:/Windows/Fonts/malgun.ttf");
//		InputStream fontStream2 = new FileInputStream("C:/Windows/Fonts/malgunbd.ttf");
//		PDType0Font fontMalgun = PDType0Font.load(doc, fontStream1);
//		PDType0Font fontMalgunBd = PDType0Font.load(doc, fontStream2);

//
//		for (PDFDto pdf : pdfList) {
//			// 페이지 추가
//			PDPage blankPage = new PDPage(PDRectangle.A4);
//			doc.addPage(blankPage);
//
//			// 현재 페이지 설정
//			PDPage page = doc.getPage(doc.getPages().getCount() - 1);
//
//			PDPageContentStream contentStream = new PDPageContentStream(doc, page);
//			drawTable(page, contentStream, new PDFont[] { fontMalgun, fontMalgunBd }, pdf);
//			contentStream.close();
//		}

//		String home = System.getProperty("user.home");
//		File pdfFile = new File(home + "/Downloads/aa.pdf");
//		ByteArrayOutputStream baos = 
		
//		OutputStream os = new FileOutputStream(pdfFile);
//		doc.save(os);
//		doc.save(pdfFile);
//		doc.close();

	}

	private void setDefault() {
		settingList = service.getSettingList();
		tagList = service.getTagList();
	}
	
	
	private PDDocument makeDocument(String pdfBody) throws Exception {
		// 문서 만들기
		final PDDocument doc = new PDDocument();
		
		// 한글폰트
//		InputStream fontStream1 = new FileInputStream("data/font/MALGUN.TTF");
//		InputStream fontStream2 = new FileInputStream("data/font/MALGUNBD.TTF");
		InputStream fontStream1 = new ClassPathResource("data/font/MALGUN.TTF").getInputStream();
		InputStream fontStream2 = new ClassPathResource("data/font/MALGUNBD.TTF").getInputStream();
		PDType0Font fontMalgun = PDType0Font.load(doc, fontStream1);
		PDType0Font fontMalgunBd = PDType0Font.load(doc, fontStream2);
		
		ArrayList<PDFDto> pdfList = parsePdf(pdfBody);
		for (PDFDto pdf : pdfList) {
			// 페이지 추가
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);

			// 현재 페이지 설정
//			PDPage page = doc.getPage(doc.getPages().getCount() - 1);

			PDPageContentStream contentStream = new PDPageContentStream(doc, page);
			drawPdf(page, contentStream, new PDFont[] { fontMalgun, fontMalgunBd }, pdf);
			contentStream.close();
		}
//		while (pdfBody.contains("<page")) {
//			String pageStr = pdfBody.substring(pdfBody.indexOf("<page"), pdfBody.indexOf("</page>"));
//			pdfBody = pdfBody.substring(pdfBody.indexOf("</page>") + "</page>".length());
//
//			PDPage page = new PDPage(PDRectangle.A4);
//			doc.addPage(page);
//
//			// 현재 페이지 설정
////			PDPage page = doc.getPage(doc.getPages().getCount() - 1);
//			PDPageContentStream contentStream = new PDPageContentStream(doc, page);
//			
////			drawTable(page, contentStream, new PDFont[] { fontMalgun, fontMalgunBd }, pdf);
////			parsePdf(pageStr, "page", page, contentStream, new PDFont[] { fontMalgun, fontMalgunBd });
//			contentStream.close();
//		}

		return doc;
	}
	
	private ArrayList<PDFDto> parsePdf(String pdfBody) throws IOException {
		ArrayList<PDFDto> pdfList = new ArrayList<>();
		String[] tagList = {"text", "table"};
		
		int sPageIdx;
		while ((sPageIdx = pdfBody.indexOf("<page")) != -1) {
			int ePageIdx = pdfBody.indexOf("</page>");
			if (ePageIdx == -1) {
				// page 닫는태그 에러처리
			}
			else {
				String pageSetting = pdfBody.substring(sPageIdx + "<page".length(), pdfBody.indexOf(">"));
				String pageBodyStr = pdfBody.substring(pdfBody.indexOf(">"), pdfBody.indexOf("</page>"));
				pdfBody = pdfBody.substring(ePageIdx + "</page>".length());
				
				PDFDto pdfDto = new PDFDto();
				pdfDto.setPageNum(pdfList.size());
				pdfDto.setPageSetting(parseSetting(pageSetting));
				pdfDto.setContents(new ArrayList<ContentDto>());

				while (pageBodyStr != null && pageBodyStr.length() != 0) {
					int sTagIdx = -1;
					int sTag = -1;
					for (int i = 0; i < tagList.length; i++) {
						int tmpIdx = pageBodyStr.indexOf("<" + tagList[i]);
						if (tmpIdx != -1) {
							if (sTagIdx == -1 || sTagIdx > tmpIdx) {
								sTagIdx = tmpIdx;
								sTag = i;
							}
						}
					}

					if (sTagIdx != -1) {
						int eTagIdx = pageBodyStr.indexOf("</" + tagList[sTag] +  ">");
						if (eTagIdx == -1) {
							// 닫는태그 에러처리
						}
						else {
							String contentStr = pageBodyStr.substring(sTagIdx, pageBodyStr.indexOf("</" + tagList[sTag] + ">") + ("</" + tagList[sTag] + ">").length());
							pageBodyStr = pageBodyStr.substring(eTagIdx + ("</" + tagList[sTag] + ">").length());
							ArrayList<ContentDto> contentDto = pdfDto.getContents();
							contentDto.add(getContent(contentStr, tagList[sTag]));
						}
					}
				}
				
				pdfList.add(pdfDto);
			}
		}
		return pdfList;
	}
	
	private ContentDto getContent(String contentStr, String type) {
		
		String settingStr = contentStr.substring(contentStr.indexOf("<" + type) + ("<" + type).length(), contentStr.indexOf(">"));

		if (contentStr.indexOf("</" + type + ">") == -1) {
			// 닫는태그 에러처리
		}
		else {
			if (type.equals("text")) {
				TextDto textDto = new TextDto();
				
				textDto.setContentSetting(parseSetting(settingStr));
				textDto.setContentType(type);

				textDto.setContent(contentStr.substring(contentStr.indexOf(">") + 1, contentStr.indexOf("</" + type + ">")));
				return textDto;
			}
			else if (type.equals("table")) {
				TableDto tableDto = new TableDto();

				HashMap<String, Object> defaultSetting = getDefaultSetting("table");
				HashMap<String, Object> inputSetting = parseSetting(settingStr);
				for (String key : defaultSetting.keySet()) {
					if (inputSetting.get(key) == null) {
						inputSetting.put(key, defaultSetting.get(key));
					}
				}
				tableDto.setContentSetting(inputSetting);
				tableDto.setContentType(type);

				int row = 0;
				int col = 0;
				ArrayList<ArrayList<TextDto>> cells = new ArrayList<ArrayList<TextDto>>();
			
				contentStr = contentStr.substring(contentStr.indexOf(">") + 1, contentStr.indexOf("</table>"));
				String[] trList = contentStr.split("</tr>");
				
				for (String tr : trList) {

					ArrayList<TextDto> rowArr = new ArrayList<TextDto>();
					row++;
					int subCol = 0;
					
					// 추후 tr setting 처리 필요
					
					tr = tr.substring(tr.indexOf(">") + 1);
					String[] tdList = tr.split("</td>");
					
					for (String td : tdList) {
						
						TextDto tdDto = new TextDto();
						String tdSettingStr = td.substring(td.indexOf("<td") + ("<td").length(), td.indexOf(">"));
						String tdText = td.substring(td.indexOf(">") + 1);
						
						HashMap<String, Object> tdDefaultSetting = getDefaultSetting("td");
						HashMap<String, Object> tdDnputSetting = parseSetting(tdSettingStr);
						for (String key : tdDefaultSetting.keySet()) {
							if (tdDnputSetting.get(key) == null) {
								tdDnputSetting.put(key, tdDefaultSetting.get(key));
							}
						}
						
						tdDto.setContentSetting(tdDnputSetting);
						tdDto.setContent(tdText);
						tdDto.setContentType("text");
						
						Integer colspan = (Integer)tdDto.getContentSetting().get("colspan");
						if (colspan != null) {
							subCol += colspan;
						}
						else {
							subCol++;
						}
						
						rowArr.add(tdDto);
					}
					
					if (col < subCol) {
						col = subCol;
					}
					cells.add(rowArr);
				}
//				while (contentStr.contains("<tr")) {
//					ArrayList<TextDto> rowArr = new ArrayList<TextDto>();
//					row++;
//					int subCol = 0;
//					// end tag 검사
//					while (contentStr.contains("<td")) {
//						// end tag 검사
//
//						cells.add(rowArr);
//						contentStr = contentStr.substring(contentStr.indexOf("</td>") + "</td>".length());
//					}
//					cells.add(rowArr);
//					contentStr = contentStr.substring(contentStr.indexOf("</tr>") + "</tr>".length());
//				}
				
				tableDto.setTableRow(row);
				tableDto.setTableCol(col);
				tableDto.setCells(cells);
				
				return tableDto;
//				return null;
			}
		}
		
		return null;
	}
	
	private String[] splitStr(String str, String tag) {
//		String pageSetting = pdfBody.substring(sPageIdx + "<page".length(), pdfBody.indexOf(">"));
//		String pageBodyStr = pdfBody.substring(pdfBody.indexOf(">"), pdfBody.indexOf("</page>"));
//		pdfBody = pdfBody.substring(ePageIdx + "</page>".length());
		return null;
	}
//	private void parsePdf(String pageStr, String tag, PDPage page, PDPageContentStream contentStream, PDFont[] font) throws Exception {
////		drawElement(pageStr, page, contentStream, font, null);//, PDFDto pdf)
//		drawElement(page, contentStream, font, null);
//		
////		String[] tagList = {"text", "table"}; // db 에서 가져온 데이터로 수정 필요
////		ArrayList<PDFDto> pdfList = new ArrayList<>();
////		ArrayList<String> tagCheck = new ArrayList<>();
////		
////		TableDto table = null;
////		PDFDto pdfDto = null;
////		HashMap<String, Object> rowSetting = new HashMap<>();
////		int row = 0;
////		int col = 0;
////		while (pageStr != null && pageStr.length() != 0) {
////
////			int idx = -1;
////			int tagIdx = -1;
////			for (int i = 1; i < tagList.length; i++) {
////				int tmpIdx = pageStr.indexOf("<" + tagList[i]);
////				if (tmpIdx != -1) {
////					if (idx == -1 || idx > tmpIdx) {
////						idx = tmpIdx;
////						tagIdx = i;
////					}
////				}
////			}
////			
////			if (idx != -1) {
////				// 태그검사
////				tagCheck.add(tagList[tagIdx]);
////				String tag = tagList[tagIdx];
////				int sIdx = pageStr.indexOf("<" + tag);
////				int eIdx = pageStr.indexOf(">");
////				
////				String tagSetting = pageStr.substring(sIdx + 1, eIdx);
////				pageStr = pageStr.substring(eIdx + 1);
////
////				HashMap<String, Object> settingMap = parseSetting(tagSetting);
////
////				if (tag.equals("text")) {
////					int tmpIdx = pageStr.indexOf("</" + tagList[tagIdx] + ">");
//////                    String title = pdfBody.substring(0, tmpIdx);
////
////					TextDto title = new TextDto();
////
////					title.setContent(pageStr.substring(0, tmpIdx));
////					title.setSetting(settingMap);
////					pdfDto.setTitle(title);
////					pdfDto.getContentSort().add(title.toString());
////				} else if (tag.equals("table")) {
////					table = new TableDto();
////					pdfDto.addTable(table);
////					table.setTableSetting(settingMap);
////					table.setContents(new ArrayList<TextDto>());
////					pdfDto.getContentSort().add(table.toString());
////				}
////			} else {
////				break;
////			}
////
////		}
//	}
	
	private static HashMap<String, Object> getDefaultSetting(String tagType) {
		HashMap<String, Object> map = new HashMap<>();

		String[] settingArr = {};
		if (tagType.equals("page")) {
			settingArr = new String[] { "padding=20", "border=0f" };
		} else if (tagType.equals("text")) {
			settingArr = new String[] { "padding=10", "size=18", "font=bold", "align=center" };
		} else if (tagType.equals("table")) {
			settingArr = new String[] { "padding=10,10,30,10", "size=12", "align=left", "color=black", "border=0.5f",
					"edge_border=1f", "border_color=black"};
		} else if (tagType.equals("td")) {
			settingArr = new String[] { "padding=10", "size=12", "font=normal", "font_weight=normal", "font_size=12", "text_align=left", "border=1", "border_color=black", "edge_border=1"};
		} else if (tagType.equals("th")) {
			settingArr = new String[] { "padding=10", "size=12", "font=bold" };
		} else if (tagType.equals("none")) {
			settingArr = new String[] { "padding=0", "size=0", "align=left", "font=normal", "border=0" };
		}

		for (String setting : settingArr) {
			String[] keyValue = setting.split("=");
			String key = keyValue[0];
			String value = keyValue[1];
			map.put(key, getValueObject(key, value));
		}
		return map;
	}

	private static HashMap<String, Object> parseSetting(String str) {

		HashMap<String, Object> map = new HashMap<>();
		String[] settingList = str.split(" ");
		boolean modify = false;

		for (String setting : settingList) {

			if (setting == null || setting.length() == 0) {
				continue;
			}

			if (!setting.contains("=")) {
				map = getDefaultSetting(setting);
			} else {
				String[] keyValue = setting.split("=");

				String key = keyValue[0];
				String value = keyValue[1].replaceAll("\"|'", "");
				map.put(key, getValueObject(key, value));
				if (key.equals("border") || key.equals("border_color")) {
					modify = true;
				}
			}
		}
		map.put("modify", modify);
		return map;
	}

	public static Object getValueObject(String key, String value) {
		if (key.equals("width")) {
			try {
				if (value.contains("%")) {
					return value;
				}
				else {
					value = value.replace("px", "");
					return Integer.parseInt(value);
				}
			} catch (NumberFormatException e) {
				System.out.println("parseIntErrorWideh");
			}
		}
		else if (key.equals("font_size") || key.equals("size") || key.equals("valueI") || key.equals("rowspan")
				|| key.equals("colspan")) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				System.out.println("parseIntError1");
			}
		} else if (key.equals("border_color")) {
			try {
				String[] color = new String[4];
				String[] colorStr = value.split(",");
				for (int i = 0; i < colorStr.length; i++) {
					if (i == 0) {
						color = new String[] { colorStr[i], colorStr[i], colorStr[i], colorStr[i] };
					} else if (i == 1) {
						color[i] = colorStr[i];
						color[i + 2] = colorStr[i];
					} else {
						color[i] = colorStr[i];
					}
				}
				return color;
			} catch (NumberFormatException e) {
				System.out.println("parseIntError2");
			}
		} else if (key.equals("padding") || key.equals("border") || key.equals("edge_border")) {
			try {
				float[] floatArr = new float[4];
				String[] paddingStr = value.split(",");
				for (int i = 0; i < paddingStr.length; i++) {
					float m = Float.parseFloat(paddingStr[i]);
					if (i == 0) {
						floatArr = new float[] { m, m, m, m };
					} else if (i == 1) {
						floatArr[i] = m;
						floatArr[i + 2] = m;
					} else {
						floatArr[i] = m;
					}
				}
				return floatArr;
			} catch (NumberFormatException e) {
				System.out.println("parseFloatError");
			}
		}
		return value;
	}

	private static void drawText(String text, PDFont font, int fontSize, float left, float bottom,
			PDPageContentStream contentStream, String color) throws Exception {
		contentStream.beginText();
		contentStream.setFont(font, fontSize);
		contentStream.newLineAtOffset(left, bottom);
//           contentStream.drawImage(pdImage, 0, 0, 595, 842);
		contentStream.setNonStrokingColor(Color.BLACK);
		// 텍스트 색상
		if (color != null) {
			if (color.toLowerCase().equals("blue")) {
				contentStream.setNonStrokingColor(Color.BLUE);
			} else if (color.toLowerCase().equals("red")) {
				contentStream.setNonStrokingColor(Color.RED);
			}
		}

		contentStream.showText(text);
		contentStream.endText();
	}

	private static void drawLine(PDPageContentStream contentStream, float xStart, float xEnd, float yStart, float yEnd,
			float border, String color) throws IOException {
		contentStream.moveTo(xStart, yStart);
		contentStream.lineTo(xEnd, yEnd);

		contentStream.setStrokingColor(Color.BLACK);
		// 텍스트 색상
		if (color != null) {
			if (color.toLowerCase().equals("blue")) {
				contentStream.setStrokingColor(Color.BLUE);
			} else if (color.toLowerCase().equals("red")) {
				contentStream.setStrokingColor(Color.RED);
			}
		}
		contentStream.setLineWidth(border);
		contentStream.stroke();
	}

	@SuppressWarnings("unchecked")
	private static void drawElement(PDPage page, PDPageContentStream contentStream, PDFont[] font, PDFDto pdf)
			throws Exception {

		float maxHeight = PDRectangle.A4.getHeight();
		float maxWidth = PDRectangle.A4.getWidth();

		HashMap<String, Object> pageSetting = pdf.getPageSetting();

		float[] pPoint = (float[]) pageSetting.get("padding");
		pPoint[0] = maxHeight - pPoint[0];
		pPoint[1] = maxWidth - pPoint[1];

		float[] pBorder = (float[]) pageSetting.get("border");
//      float yPoint = pPoint[0];

		if (pBorder[0] + pBorder[1] + pBorder[2] + pBorder[3] > 0) {
			drawLine(contentStream, pPoint[1], pPoint[3], pPoint[0], pPoint[0], pBorder[0], null);
			drawLine(contentStream, pPoint[3], pPoint[3], pPoint[0], pPoint[2], pBorder[1], null);
			drawLine(contentStream, pPoint[1], pPoint[3], pPoint[2], pPoint[2], pBorder[2], null);
			drawLine(contentStream, pPoint[1], pPoint[1], pPoint[0], pPoint[2], pBorder[3], null);
		}
//      drawLine(contentStream, (pPoint[1] + pPoint[3]) / 2,  (pPoint[1] + pPoint[3]) / 2, pPoint[0], pPoint[2], 1f);

//		TextDto titleDto = pdf.getTitle();
//		if (titleDto != null) {
//
//			String title = titleDto.getContent();
//			int size = (int) titleDto.getContentSetting().get("size");
//			float[] padding = (float[]) titleDto.getContentSetting().get("padding");
//			float fontHeight = font[0].getFontDescriptor().getFontBoundingBox().getHeight() / 1000f;
//
//			float[] textArea = pPoint.clone();
//			textArea[2] = textArea[0] - (padding[0] + padding[2]);
//			textArea[2] -= (fontHeight * title.split("<br>").length * size);
//			parseText(title, titleDto.getContentSetting(), font, textArea, contentStream);
//
//			pPoint[0] = textArea[2];
//		}
//      drawLine(contentStream, pPoint[3], pPoint[1], pPoint[0], pPoint[0], pBorder); // 테스트용 가운데 선

//		for (TableDto tableDto : pdf.getTableList()) {
//			ArrayList<TextDto> textList = tableDto.getContents();
//			if (textList != null && textList.size() > 0) {
//				float[][] tmp = calcPoint(tableDto, pPoint, font);
//				float[] xList = tmp[0];
//				float[] yList = tmp[1];
//				ArrayList<LineDto> line = lineList(tableDto);
//
//				for (int i = 0; i < line.size(); i++) {
//					LineDto l = line.get(i);
//					drawLine(contentStream, xList[l.getStartX()], xList[l.getEndX()], yList[l.getStartY()],
//							yList[l.getEndY()], l.getBorder(), l.getBorderColor());
//					if (pPoint[0] > yList[l.getEndY()]) {
//						pPoint[0] = yList[l.getEndY()];
//					}
//				}
//
//				// 텍스트 처리
//				for (TextDto text : textList) {
//					float[] textArea = new float[4];
//
//					int row = text.getRow();
//					int col = text.getCol();
//
//					int colspan = 0;
//					int rowspan = 0;
//					if (text.getContentSetting().get("colspan") != null) {
//						colspan = (int) text.getContentSetting().get("colspan") - 1;
//					}
//					if (text.getContentSetting().get("rowspan") != null) {
//						rowspan = (int) text.getContentSetting().get("rowspan") - 1;
//					}
//
//					textArea[0] = yList[row - 1];
//					textArea[1] = xList[col + colspan];
//					textArea[2] = yList[row + rowspan];
//					textArea[3] = xList[col - 1];
//
//					if (text.getContent() != null) {
//						parseText(text.getContent(), text.getContentSetting(), font, textArea, contentStream);
//					}
//				}
//
//				pPoint[0] -= ((float[]) tableDto.getContentSetting().get("padding"))[2];
//			}
//		}

	}
	
	@SuppressWarnings("unchecked")
	private static void drawPdf(PDPage page, PDPageContentStream contentStream, PDFont[] font, PDFDto pdf)
			throws Exception {

		float maxHeight = PDRectangle.A4.getHeight();
		float maxWidth = PDRectangle.A4.getWidth();
		System.out.println(maxHeight + " : " + maxWidth);

		HashMap<String, Object> pageSetting = pdf.getPageSetting();

		float[] pPoint = (float[]) pageSetting.get("padding");
		pPoint[0] = maxHeight - pPoint[0];
		pPoint[1] = maxWidth - pPoint[1];

		float[] pBorder = (float[]) pageSetting.get("border");
//      float yPoint = pPoint[0];

		// 페이지 테두리
		if (pBorder != null && pBorder[0] + pBorder[1] + pBorder[2] + pBorder[3] > 0) {
			drawLine(contentStream, pPoint[1], pPoint[3], pPoint[0], pPoint[0], pBorder[0], null);
			drawLine(contentStream, pPoint[3], pPoint[3], pPoint[0], pPoint[2], pBorder[1], null);
			drawLine(contentStream, pPoint[1], pPoint[3], pPoint[2], pPoint[2], pBorder[2], null);
			drawLine(contentStream, pPoint[1], pPoint[1], pPoint[0], pPoint[2], pBorder[3], null);
		}
//      drawLine(contentStream, (pPoint[1] + pPoint[3]) / 2,  (pPoint[1] + pPoint[3]) / 2, pPoint[0], pPoint[2], 1f);

		ArrayList<ContentDto> contents = pdf.getContents();
		for (ContentDto con : contents) {
			if (con.getContentType().equals("text")) {
				TextDto textDto = (TextDto)con;
				
				String text = textDto.getContent();
				int size = (int) textDto.getContentSetting().get("font_size");
				float[] padding = (float[]) textDto.getContentSetting().get("padding");
				float fontHeight = font[0].getFontDescriptor().getFontBoundingBox().getHeight() / 1000f;
	
				float[] textArea = pPoint.clone();
				textArea[2] = textArea[0] - (padding[0] + padding[2]);
				textArea[2] -= (fontHeight * text.split("<br>").length * size);
				parseText(text, textDto.getContentSetting(), font, textArea, contentStream);
	
				pPoint[0] = textArea[2];
			}
			else if (con.getContentType().equals("table")) {
				TableDto tableDto = (TableDto)con;
				ArrayList<ArrayList<TextDto>> cells = tableDto.getCells();
				
				if (cells != null && cells.size() > 0) {
					float[][] tmp = calcPoint(tableDto, pPoint, font);
					float[] xList = tmp[0];
					float[] yList = tmp[1];
					ArrayList<LineDto> line = lineList(tableDto);
	
					for (int i = 0; i < line.size(); i++) {
						LineDto l = line.get(i);
						
						drawLine(contentStream, xList[l.getStartX()], xList[l.getEndX()], yList[l.getStartY()],
								yList[l.getEndY()], l.getBorder(), l.getBorderColor());
						if (pPoint[0] > yList[l.getEndY()]) {
							pPoint[0] = yList[l.getEndY()];
						}
					}
	
					// 텍스트 처리
					int row = 0;
					boolean[][] spanMatrix = new boolean[tableDto.getTableRow()][tableDto.getTableCol()];
					
					for (ArrayList<TextDto> textList : cells) {
						int col = 0;
						for (TextDto text : textList) {
							float[] textArea = new float[4];
							
							while (spanMatrix[row][col]) {
								col++;
							}
		
							int colspan = 1;
							int rowspan = 1;
							if (text.getContentSetting().get("rowspan") != null) {
								rowspan = (int) text.getContentSetting().get("rowspan");
								for (int r = 0; r < rowspan; r++) {
									spanMatrix[row + r][col] = true;
								}
							}
							if (text.getContentSetting().get("colspan") != null) {
								colspan = (int) text.getContentSetting().get("colspan");
								
								for (int r = 0; r < rowspan; r++) {
									for (int c = 0; c < colspan; c++) {
										spanMatrix[row + r][col + c] = true;
									}
								}
							}
		
							textArea[0] = yList[row];
							textArea[1] = xList[col + colspan];
							textArea[2] = yList[row + rowspan];
							textArea[3] = xList[col];
		
							if (text.getContent() != null) {
								parseText(text.getContent(), text.getContentSetting(), font, textArea, contentStream);
							}
							col++;
						}
						row++;
					}
	
					pPoint[0] -= ((float[]) tableDto.getContentSetting().get("padding"))[2];
				}
			}
		}
//		TextDto titleDto = pdf.getTitle();
//		if (titleDto != null) {
//
//			String title = titleDto.getContent();
//			int size = (int) titleDto.getContentSetting().get("size");
//			float[] padding = (float[]) titleDto.getContentSetting().get("padding");
//			float fontHeight = font[0].getFontDescriptor().getFontBoundingBox().getHeight() / 1000f;
//
//			float[] textArea = pPoint.clone();
//			textArea[2] = textArea[0] - (padding[0] + padding[2]);
//			textArea[2] -= (fontHeight * title.split("<br>").length * size);
//			parseText(title, titleDto.getContentSetting(), font, textArea, contentStream);
//
//			pPoint[0] = textArea[2];
//		}
//      drawLine(contentStream, pPoint[3], pPoint[1], pPoint[0], pPoint[0], pBorder); // 테스트용 가운데 선

//		for (TableDto tableDto : pdf.getTableList()) {
//			ArrayList<TextDto> textList = tableDto.getContents();
//			if (textList != null && textList.size() > 0) {
//				float[][] tmp = calcPoint(tableDto, pPoint, font);
//				float[] xList = tmp[0];
//				float[] yList = tmp[1];
//				ArrayList<LineDto> line = lineList(tableDto);
//
//				for (int i = 0; i < line.size(); i++) {
//					LineDto l = line.get(i);
//					drawLine(contentStream, xList[l.getStartX()], xList[l.getEndX()], yList[l.getStartY()],
//							yList[l.getEndY()], l.getBorder(), l.getBorderColor());
//					if (pPoint[0] > yList[l.getEndY()]) {
//						pPoint[0] = yList[l.getEndY()];
//					}
//				}
//
//				// 텍스트 처리
//				for (TextDto text : textList) {
//					float[] textArea = new float[4];
//
//					int row = text.getRow();
//					int col = text.getCol();
//
//					int colspan = 0;
//					int rowspan = 0;
//					if (text.getContentSetting().get("colspan") != null) {
//						colspan = (int) text.getContentSetting().get("colspan") - 1;
//					}
//					if (text.getContentSetting().get("rowspan") != null) {
//						rowspan = (int) text.getContentSetting().get("rowspan") - 1;
//					}
//
//					textArea[0] = yList[row - 1];
//					textArea[1] = xList[col + colspan];
//					textArea[2] = yList[row + rowspan];
//					textArea[3] = xList[col - 1];
//
//					if (text.getContent() != null) {
//						parseText(text.getContent(), text.getContentSetting(), font, textArea, contentStream);
//					}
//				}
//
//				pPoint[0] -= ((float[]) tableDto.getContentSetting().get("padding"))[2];
//			}
//		}

	}

	private static void parseText(String rawText, HashMap<String, Object> settingMap, PDFont[] font, float[] textArea,
			PDPageContentStream stream) throws Exception {
		
		HashMap<String, Object> setting = settingMap;
//		String[] textArr = rawText.split("<br>");
		ArrayList<String> textArr = new ArrayList<String>();

		PDFont tFont = setting.get("font_weight").equals("bold") ? font[1] : font[0];
		int tSize = (int) setting.get("font_size");
		float[] tpadding = (float[]) setting.get("padding");
		
		// 라인 내 개행 처리
		float innerWidth = (textArea[1] - textArea[3]) - (tpadding[1] + tpadding[3]);
		for (String str : rawText.split("<br>")) {
			float strWidth;
			while ((strWidth = getStringWidth(str, tFont, tSize)) > innerWidth) {
				int splitIdx = (int)(str.length() * (innerWidth /strWidth) / 1);
				// 문자열 split 인덱스 조정
				while (true) {
					if (getStringWidth(str.substring(0, splitIdx), tFont, tSize) > innerWidth) {
						splitIdx--;
					}
					else if (getStringWidth(str.substring(0, splitIdx + 1), tFont, tSize) < innerWidth) {
						splitIdx++;
					}
					else {
						break;
					}
				}
				textArr.add(str.substring(0, splitIdx));
				str = str.substring(splitIdx);
			}
			textArr.add(str);
		}
		
		String align = (String) setting.get("text_align");
		String color = (String) setting.get("color");

		float stringHeight = tFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f;
//       float yF = textArea[0] - tpadding[0];//

		// 개행된 단락을 위한 Y값 계산
		// tSize 라인별로 다른경우 처리해야하지 않을까 ....
		float yF = (textArea[0] + textArea[2] + ((stringHeight * tSize) * textArr.size())) / 2
				- 2f;
//		float yF = (textArea[0] + textArea[2] + ((stringHeight * tSize) * textArr.size()) + (1f * textArr.size())) / 2
//				- 2f;

//		float yF = (textArea[0] + textArea[2] + ((stringHeight * tSize) * textArr.size()) + (textArr.size())) / 2
//				+ 2f;
		
		for (String t : textArr) {
			ArrayList<HashMap<String, Object>> subContent = parseTag(t);

			float conWidth = 0;
			int conMaxSize = 0;
			int subSize;
			for (HashMap<String, Object> map : subContent) {
				align = map.get("text_align") != null ? (String) map.get("text_align") : align;
				String text = (String) map.get("content");
				PDFont subFont = map.get("bold") != null ? font[1] : tFont;
				subSize = map.get("size") != null ? (int) map.get("size") : tSize;
				conMaxSize = conMaxSize < subSize ? subSize : conMaxSize;
				conWidth += getStringWidth(text, subFont, subSize);
			}

			float xF = textArea[3] + tpadding[3];

			if (align.equals("center") || align.equals("right")) {
				xF = textArea[1] - conWidth;

				if (align.equals("center")) {
					xF = (xF - textArea[3]) / 2 + textArea[3];
				} else if (align.equals("right")) {
					xF -= tpadding[1];
				}
			}
			yF -= ((stringHeight * conMaxSize) - 1f);
//			yF -= ((stringHeight * conMaxSize));
			textArea[0] -= yF;

			for (int i = 0; i < subContent.size(); i++) {
				HashMap<String, Object> subCon = subContent.get(i);
				String text = (String) subCon.get("content");
				PDFont subFont = subCon.get("bold") != null ? font[1] : tFont;
				subSize = subCon.get("size") != null ? (int) subCon.get("size") : tSize;
				String subColor = subCon.get("color") != null ? (String) subCon.get("color") : color;
				drawText(text, subFont, subSize, xF, yF, stream, subColor);

				if (subCon.get("line") != null) { // 밑줄
					String c = (String) subCon.get("line") == "true" ? subColor : (String) subCon.get("line");
					drawLine(stream, xF, xF + getStringWidth(text, subFont, subSize), yF - 2f, yF - 2f, 1f, c);
				}
				xF += getStringWidth(text, subFont, subSize);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<HashMap<String, Object>> parseTag(String text) {

		ArrayList<HashMap<String, Object>> subList = new ArrayList<>();
		HashMap<String, Object> map = new HashMap<>();
		map.put("content", text);
		subList.add(map);

		int sIdx = -1;
		int eIdx = -1;
		String[] tagList = new String[] { "line", "bold", "size", "color", "align" }; // 검사할 태그 리스트

		for (int i = 0; i < subList.size(); i++) {
			while (true) {
				int idx = -1;
				String tag = "";
				HashMap<String, Object> c = subList.get(i);
				String con = (String) c.get("content");

				for (String t : tagList) {
					int tmpIdx = con.indexOf("<" + t);

					if (tmpIdx != -1) {
						if (idx == -1 || idx > tmpIdx) {
							idx = tmpIdx;
							tag = t;
						}
					}

				}
				if (idx != -1) { // 스타일 관련 태그 존재

					String sTag = "<" + tag;
					String eTag = "</" + tag + ">";

					sIdx = con.indexOf(sTag);
					int tempIdx = con.substring(sIdx).indexOf(">") + sIdx;

					String tagSetting = con.substring(sIdx + 1, tempIdx);
					sTag = "<" + tagSetting + ">";

					eIdx = con.replace(sTag, "").indexOf(eTag);
					con = con.replace(sTag, "").replace(eTag, "");

					HashMap<String, Object> subMap1 = (HashMap<String, Object>) c.clone();
					subMap1.put("content", con.substring(0, sIdx));

					HashMap<String, Object> subMap2 = (HashMap<String, Object>) c.clone();

					if (tagSetting.indexOf("=") != -1) {
						String[] arr1 = tagSetting.split(" ");
						String key = "";
						for (String s : arr1) {
							if (!s.contains("=")) {
								key = s;
							} else {
								String[] arr2 = s.split("=");

								String k = arr2[0];

								String v = arr2[1].replaceAll("\"|'", "");
								subMap2.put(key, getValueObject(key, v));
							}
						}
					} else {
						subMap2.put(tag, true);
					}
					subMap2.put("content", con.substring(sIdx, eIdx));

					HashMap<String, Object> subMap3 = (HashMap<String, Object>) c.clone();
					subMap3.put("content", con.substring(eIdx));

					subList.remove(i);
					subList.add(i, subMap1);
					subList.add(i + 1, subMap2);
					subList.add(i + 2, subMap3);
				} else {
					break;
				}
			}
		}

		for (int i = 0; i < subList.size(); i++) {
			String content = (String) subList.get(i).get("content");
			if (content == null || content.length() == 0) {
				subList.remove(i--);
			}
		}
		return subList;
	}

	// 테이블 교차점 (좌상단 > 우하단 순, 이전 교차점과의 거리값)
	// 좌표 = 31 * 실제픽셀값 / 41로 계산
	private static float[][] calcPoint(TableDto tableDto, float[] point, PDFont[] font) throws IOException {
		float[] tablePadding = (float[]) tableDto.getContentSetting().get("padding");

		float perWidth = (point[1] - point[3] - tablePadding[3] - tablePadding[1]) / 100;
		float cellPer = 100f / tableDto.getTableCol();
		float[] xList = new float[tableDto.getTableCol() + 1];
		float[] yList = new float[tableDto.getTableRow() + 1];
		xList[0] = point[3] + tablePadding[3];
		yList[0] = point[0] - tablePadding[0];

		ArrayList<ArrayList<TextDto>> cells = tableDto.getCells();
		int remainCell = 0;
		float remainPer = 100f;
		// col size 계산
		for (int i = 0; i < cells.get(0).size(); i++) {
			TextDto tmpDto = cells.get(0).get(i);
			HashMap<String, Object> setting = tmpDto.getContentSetting();
			int col = i + 1;

			// 전체 width 넘는지 검사하여 continue
			if (setting.get("width") instanceof String && ((String)setting.get("width")).length() != 0) {
				float per = Integer.parseInt(((String)setting.get("width")).replace("%", ""));
				if (per <= remainPer) {
					xList[col] = per * perWidth;
					remainPer -= per;
				}
			}
			else if (setting.get("width") instanceof Integer) {
				float per = (int)setting.get("width") / perWidth;
				if (per <= remainPer) {
					xList[col] = (int)setting.get("width");
					remainPer -= per;
				}
			}
			else {
				remainCell++;
			}
		}
		for (int i = 1; i < xList.length; i++) {
			if (xList[i] == 0) {
				xList[i] = perWidth * remainPer / remainCell;
			}
			xList[i] += xList[i - 1];
		}
		
		// row size 계산
		for (int i = 0; i < cells.size(); i++) {

			ArrayList<TextDto> trList = cells.get(i);
			for (int j = 0; j < trList.size(); j++) {

				TextDto tmpDto = trList.get(j);
				if (tmpDto.getContent() == null) {
					continue;
				}

				HashMap<String, Object> setting = tmpDto.getContentSetting();
				float[] padding = (float[]) setting.get("padding");
				int rowspan = setting.get("rowspan") != null ? (int)setting.get("rowspan") : 1;
//				Integer rowspan = (Integer) setting.get("rowspan");
				
				// 넓이 계산
				int row = i + 1;
				int col = j + 1;

				// 높이 계산
				float cellHeight = padding[0] + padding[2];
				String[] titleArr = tmpDto.getContent().split("<br>"); // 개행 문자열 처리 필요
				float stringHeight = font[0].getFontDescriptor().getFontBoundingBox().getHeight() / 1000f;
//				if (tmpDto.getContentSetting().get("rowspan") != null) {
//					continue;
//				}
				for (String title : titleArr) {
					ArrayList<HashMap<String, Object>> subContent = parseTag(title);
					int tSize = (int) tmpDto.getContentSetting().get("size");
					int subSize;
					int conMaxSize = 0;
					for (HashMap<String, Object> s : subContent) {
						subSize = s.get("size") != null ? (int) s.get("size") : tSize;
						conMaxSize = conMaxSize < subSize ? subSize : conMaxSize;
					}
					
					float tdWidth = (xList[col] - xList[col - 1]) - (padding[1] + padding[3]);
					float strWidth = getStringWidth(title, font[0], tSize);
					
					int lineCnt = 1;
					if (tdWidth < strWidth) {
						lineCnt += strWidth / tdWidth;
					}
					
					cellHeight += (stringHeight * conMaxSize * lineCnt);// + 1f;
				}
				
				for (int k = 0; k < rowspan; k++) {
					if (yList[row + k] < cellHeight / rowspan) {
						yList[row + k] = cellHeight / rowspan;
					}
				}
			}
		}

		for (int i = 1; i < yList.length; i++) {
			yList[i] = yList[i - 1] - yList[i];
		}
		return new float[][] { xList, yList };
	}

//	private static float[][] calcPoint(TableDto tableDto, float[] point, PDFont[] font) throws IOException {
//		float[] tablePadding = (float[]) tableDto.getContentSetting().get("padding");
//
//		float perWidth = (point[1] - point[3] - tablePadding[3] - tablePadding[1]) / 100;
//		float cellPer = 100f / tableDto.getTableCol();
//		float[] xList = new float[tableDto.getTableCol() + 1];
//		float[] yList = new float[tableDto.getTableRow() + 1];
//		xList[0] = point[3] + tablePadding[3];
//		yList[0] = point[0] - tablePadding[0];
//
//		ArrayList<ArrayList<TextDto>> cells = tableDto.getCells();
//		int remainCell = 0;
//		for (int i = 0; i < cells.size(); i++) {
//
//			ArrayList<TextDto> trList = cells.get(i);
//			for (int j = 0; j < trList.size(); j++) {
//
//				TextDto tmpDto = trList.get(j);
//				if (tmpDto.getContent() == null) {
//					continue;
//				}
//
//				HashMap<String, Object> setting = tmpDto.getContentSetting();
//				float[] padding = (float[]) setting.get("padding");
//
//				// 넓이 계산
////				int row = tmpDto.getRow();
////				int col = tmpDto.getCol();
//				int row = i + 1;
//				int col = j + 1;
//
//				System.out.println(setting.get("width") instanceof String);
//				if (!((String)setting.get("width")).contains("%") && setting.get("width") != null && xList[col] == 0f) {
//					xList[col] = (int) setting.get("width") * perWidth;
//					remainCell++;
//					cellPer -= ((int) setting.get("width") - cellPer) / (tableDto.getTableCol() - remainCell);
//				}
//
//				// 높이 계산
//				float cellHeight = padding[0] + padding[2];
//				String[] titleArr = tmpDto.getContent().split("<br>"); // 개행 문자열 처리 필요
//				float stringHeight = font[0].getFontDescriptor().getFontBoundingBox().getHeight() / 1000f;
////				if (tmpDto.getContentSetting().get("rowspan") != null) {
////					continue;
////				}
//				for (String title : titleArr) {
//					ArrayList<HashMap<String, Object>> subContent = parseTag(title);
//					int tSize = (int) tmpDto.getContentSetting().get("size");
//					int subSize;
//					int conMaxSize = 0;
//					for (HashMap<String, Object> s : subContent) {
//						subSize = s.get("size") != null ? (int) s.get("size") : tSize;
//						conMaxSize = conMaxSize < subSize ? subSize : conMaxSize;
//					}
//					System.out.println(xList[col]);
//					
////					float tdWidth = (xList[col - 1] - xList[col]) - (padding[1] + padding[3]);
////					float strWidth = getStringWidth(title, font[0], tSize);
////					
////					System.out.println(xList[col - 1] + " : " + xList[col]);
////					System.out.println(tdWidth + ":" + strWidth);
////					int lineCnt = 1;
////					if (tdWidth < strWidth) {
////						lineCnt += strWidth / tdWidth;
////						System.out.println(title);
////						System.out.println(lineCnt);
////						System.out.println("--------");
////					}
////					// 라인 내 개행 처리
////					float innerWidth = (textArea[1] - textArea[3]) - (tpadding[1] + tpadding[3]);
////					for (String str : rawText.split("<br>")) {
////						float strWidth;
////						while ((strWidth = getStringWidth(str, tFont, tSize)) > innerWidth) {
////							int splitIdx = (int)(str.length() * (innerWidth /strWidth) / 1);
////							// 문자열 split 인덱스 조정
////							while (true) {
////								if (getStringWidth(str.substring(0, splitIdx), tFont, tSize) > innerWidth) {
////									splitIdx--;
////								}
////								else if (getStringWidth(str.substring(0, splitIdx + 1), tFont, tSize) < innerWidth) {
////									splitIdx++;
////								}
////								else {
////									break;
////								}
////							}
////							
////							textArr.add(str.substring(0, splitIdx));
////							str = str.substring(splitIdx);
////						}
////						textArr.add(str);
////					}
////					
//					cellHeight += (stringHeight * conMaxSize) + 1f;
//				}
//				if (yList[row] < cellHeight) {
//					yList[row] = cellHeight;
//				}
//			}
//		}
//
//		for (int i = 1; i < xList.length; i++) {
//			if (xList[i] == 0) {
//				xList[i] = perWidth * cellPer;
//			}
//			xList[i] += xList[i - 1];
//		}
//		for (int i = 1; i < yList.length; i++) {
//			yList[i] = yList[i - 1] - yList[i];
//		}
//		return new float[][] { xList, yList };
//	}
	
	// 화면에 그려질 라인 정보
	// 시작X, 끝X, 시작Y, 끝Y, 라인 굵기(1 : bold / 0 : normal)
	// 끝점 : 999로 표시
	private static ArrayList<LineDto> lineList(TableDto tableDto) {
		ArrayList<LineDto> lineList = new ArrayList<>();
		
		// 기본값 처리 필요
		float[] edgeBorder = (float[]) tableDto.getContentSetting().get("edge_border");

		boolean[][] spanMatrix = new boolean[tableDto.getTableRow()][tableDto.getTableCol()];
		
		int row = 0;
		ArrayList<ArrayList<TextDto>> cells = tableDto.getCells();
		for (int i = 0; i < cells.size(); i++) {
			int col = 0;
			ArrayList<TextDto> trList = cells.get(i);
			for (int j = 0; j < trList.size(); j++) {

				TextDto textDto = trList.get(j);

				if (textDto.getContent() == null) {
					continue;
				}

				while (spanMatrix[row][col]) {
					col++;
				}

				int rowspan = 1;
				if (textDto.getContentSetting().get("rowspan") != null) {
					rowspan = (int) textDto.getContentSetting().get("rowspan");
					for (int r = 0; r < rowspan; r++) {
						spanMatrix[row + r][col] = true;
					}
				}
				int colspan = 1;
				if (textDto.getContentSetting().get("colspan") != null) {
					colspan = (int) textDto.getContentSetting().get("colspan");
					
					for (int r = 0; r < rowspan; r++) {
						for (int c = 1; c < colspan; c++) {
							spanMatrix[row + r][col + c] = true;
						}
					}
//
//					System.out.println("........................");
//					System.out.println(rowspan + ":" + colspan);
//					for (boolean[] b1 : spanMatrix) {
//						for (boolean b2 : b1) {
//							System.out.print(b2 + " ");
//						}
//						System.out.println();
//					}
//					System.out.println("........................");
				}
				
				// 기본값 처리 필요
				float[] ln = ((float[]) textDto.getContentSetting().get("border")).clone();
				String[] borderColor = ((String[]) textDto.getContentSetting().get("border_color")).clone();
			
				ln[0] = row == 1 ? edgeBorder[0] : ln[0];
				ln[1] = col + colspan == tableDto.getTableCol() ? edgeBorder[1] : ln[1];
				ln[2] = row + rowspan == tableDto.getTableRow() ? edgeBorder[2] : ln[2];
				ln[3] = col == 1 ? edgeBorder[3] : ln[3];

				int startCol = col;
				int endCol = col + colspan;
				int startRow = row;
				int endRow = row + rowspan;

//				System.out.println("-----------");
//				System.out.println(textDto.getContent());
//				System.out.println(startCol + "_" + endCol);
//				System.out.println(startRow + "_" + endRow);
				lineList.add(new LineDto(endCol, endCol, endRow, startRow, ln[1], borderColor[1]));
				lineList.add(new LineDto(startCol, endCol, endRow, endRow, ln[2], borderColor[2]));
				lineList.add(new LineDto(startCol, endCol, startRow, startRow, ln[0], borderColor[0]));
				lineList.add(new LineDto(startCol, startCol, endRow, startRow, ln[3], borderColor[3]));

				col++;
			}
			row++;
		}
		return lineList;
	}

	private static float getStringWidth(String text, PDFont font, int fontSize) throws IOException {
		return font.getStringWidth(text) * fontSize / 1000F;
	}
}