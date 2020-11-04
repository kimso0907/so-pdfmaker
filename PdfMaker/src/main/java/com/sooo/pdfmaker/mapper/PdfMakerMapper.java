package com.sooo.pdfmaker.mapper;

import java.util.HashMap;
import java.util.List;

import com.sooo.pdfmaker.domain.TagStrVO;

public interface PdfMakerMapper {
	public List<TagStrVO> getTagStr();
	public List<TagStrVO> getTagList();
	public List<TagStrVO> getSettingList();
}
