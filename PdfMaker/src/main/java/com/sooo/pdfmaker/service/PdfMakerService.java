package com.sooo.pdfmaker.service;

import java.util.HashMap;
import java.util.List;

import com.sooo.pdfmaker.domain.TagStrVO;

public interface PdfMakerService {
	public List<TagStrVO> getTagStr();
	public List<TagStrVO> getTagList();
	public List<TagStrVO> getSettingList();
	public List<TagStrVO> getRefSetting();
	public List<TagStrVO> getDefSetting();
	public List<TagStrVO> getDescription();
}
