var defSizeList = {
	maxWidth : 595.27563,
	maxHeight : 841.8898,
	fontNormal : 1.1791992,
	fontBold : 1.2016602
};
var elementList = [];
var seqList = [];
var pdfScale = 0.7;
var tagList = [];
var tagStrList = [];
var defStyleList = [];
var subTagList = [];

$(document).ready(function() {
	init();
});

function init() {

	$.ajax({
		url : "/getTagStr.do",
		type : "GET",
		dataType : "json",
		contentType : "application/text; charset=UTF-8",
		success : function(result) {
			for ( var i in result.tag) {
				var idx = result.tag[i].tagCode;
				if (result.tag[i].flag == "PDF_BODY") {
					tagList[idx] = [];
					tagList[idx] = result.tag[i];
				}
				else if (result.tag[i].flag == "CSS") {
					subTagList[idx] = [];
					subTagList[idx] = result.tag[i];
				}
			}
			for ( var i in result.tagStr) {
				var idx = result.tagStr[i].strCode;
				tagStrList[idx] = [];
				tagStrList[idx] = result.tagStr[i];
			}
			for ( var i in result.setting) {
				var idx = result.setting[i].setCode;
				defStyleList[idx] = [];
				defStyleList[idx] = result.setting[i];
			}
			addBtn();
			setBtnActivation();
		},
		error : function(request, status, error) {
			console.log(error);
		}
	});
	
	addBtn();
	setBtnActivation();

	$(".div-add-element").css("width", defSizeList.maxWidth * pdfScale);
	$("#box_page").css("width", defSizeList.maxWidth * pdfScale);
	$("#box_page").css("height", defSizeList.maxHeight * pdfScale);
}

/**
 * @returns
 */
function addBtn() {

	var boxAddBtn = $("#box_addbtn");
	var cssModal = $("#css_modal");
	var keyList = [ "tagCode", "tagName" ];

	// PDF 요소 추가
	for ( var i in tagList) {
		boxAddBtn.append(applyToFormat("TAG_BTN_01", makeValueList(tagList[i],
				keyList)));
	}

	// 모달에 SUB TAG 추가
	for ( var i in subTagList) {
		cssModal.append(applyToFormat("TAG_BTN_02", makeValueList(
				subTagList[i], keyList)));
	}
}

/**
 * @param code
 * @returns
 */
function clickAddElement(code) {

	var tagCode = tagList[code].strCode;
	var treeCode = tagList[code].treeCode;
	var cssCode = tagList[code].cssCode;
	var id, pageSeq, tagName;
	var parentObj, treeObj;
	var tagValue, element;

	// 요소추가 행, 열
	if (code.indexOf("tr") != -1 || code.indexOf("td") != -1) {

		var row, col;

		parentObj = $(".sel-table");
		id = parentObj.attr("id") + "_";
		if (code.indexOf("tr") != -1) {
			treeObj = $("#" + parentObj.attr("id") + "_div");
			row = elementList[parentObj.attr("id")].row + 1;
			col = elementList[parentObj.attr("id")].col;

			tagName = tagList[code].tagName;
			elementList[parentObj.attr("id")].row = row;

		} else if (code.indexOf("td") != -1) {
			row = elementList[parentObj.attr("id")].row;
			col = elementList[parentObj.attr("id")].col + 1;
			tagName = tagList[code].tagName;
			elementList[parentObj.attr("id")].col = col;
		}

		for (var r = 0; r < row; r++) {
			var rowId = id + r;
			tagName = tagList["tr"].tagName + r;
			if ($("#" + rowId).length == 0) {
				tagValue = {
					"id" : rowId,
					"tagName" : tagName
				};
				element = {
					"cssCode" : "tr",
					"tagCode" : "tr",
					"text" : tagName,
					"row" : r
				};
				addElement("tr", tagValue, element, parentObj, treeObj);
			}
			var sCol = parentObj.children().eq(r).children().length;
			for (var c = sCol; c < col; c++) {
				var pObj = parentObj.children().eq(r);
				var tObj = $("#" + pObj.attr("id") + "_div");
				tagName = tagList["td"].tagName + r + "_" + c;
				tagValue = {
					"id" : rowId + "_" + c,
					"tagName" : tagName
				};
				element = {
					"cssCode" : "SET_TD_01",
					"tagCode" : "td",
					"text" : tagName,
					"row" : r,
					"col" : c
				};
				addElement("td", tagValue, element, pObj, tObj);
			}
		}

	} else {
		if (code.indexOf("page") != -1) {
			$(".div-sel-page").toggleClass("div-sel-page"); // 현재 표시된 페이지 숨김처리
			parentObj = $("#box_page");
			treeObj = $("#box_tree");
			pageSeq = parentObj.children().length;
			tagName = tagList[code].tagName + pageSeq;
			id = code + "_" + pageSeq;
			tagValue = {
				"id" : id,
				"tagName" : tagName
			};
			element = {
				"cssCode" : cssCode,
				"text" : tagName,
				"tagCode" : code,
				"seqNo" : pageSeq
			};
		} else {
			parentObj = $(".div-sel-page");
			treeObj = $("#" + parentObj.attr("id") + "_div");
			var listId = parentObj.attr("id");
			var eleSeqNo = elementList[listId][code].length;
			pageSeq = elementList[listId].seqNo;
			tagName = tagList[code].tagName + (eleSeqNo + 1);
			id = code + "_" + pageSeq + "_" + eleSeqNo;
			elementList[listId][code][eleSeqNo] = "tree_" + id;
			tagValue = {
				"id" : id,
				"tagName" : tagName
			};
			element = {
				"cssCode" : cssCode,
				"text" : tagName,
				"tagCode" : code
			};
		}

		addElement(code, tagValue, element, parentObj, treeObj);
		// 테이블 row, col 추가
		if (code.toLowerCase().indexOf("table") != -1) {
			elementList[id].row = 0;
			elementList[id].col = 0;

			clickAddElement("tr");
			clickAddElement("tr");
			clickAddElement("td");
			clickAddElement("td");
		}

		clickElement(id);
		setBtnActivation();
	}
}

/**
 * @param code
 * @param tagValue
 * @param element
 * @param pObj
 * @param tObj
 * @returns
 */
function addElement(code, tagValue, element, pObj, tObj) {

	var tagCode = tagList[code].strCode;
	var treeCode = tagList[code].treeCode;
	var cssCode = tagList[code].cssCode;

	elementList[tagValue.id] = element;

	for ( var i in tagList) {
		var codeToLower = code.toLowerCase();
		var parentToLower = tagList[i].parentCode.toLowerCase();
		if (parentToLower.indexOf(codeToLower) != -1) {
			elementList[tagValue.id][i] = [];
		}
	}

	pObj.append(applyToFormat(tagCode, tagValue)); // 새 페이지 추가
	tObj.append(applyToFormat(treeCode, tagValue)); // 트리추가	

	if (tagList[code].parentCode != "ROOT"
			&& $("#" + pObj.attr("id") + "_div").attr("class").indexOf(
					"div-tree-hide") != -1) {
		toggleFold(pObj.attr("id") + "_div"); // 트리 상태변경
	}

	setStyle(tagValue.id, defStyleList[cssCode], defStyleList[cssCode]);

	$(".sel-" + code).removeClass("sel-" + code);
	$("#" + tagValue.id).addClass("sel-" + code);
}

/**
 * @param id
 * @param cssValue
 * @param defValue
 * @returns
 */
function setStyle(id, cssValue, defValue) {

	for ( var i in defValue) {
		if (defValue[i].length != 0) {
			var value = cssValue[i];
				value = value == null ? defValue[i] : value;
				value = parseValue(i.replace("_", "-"), value);

			if (value == null || value.length == 0) {
				continue;
			}
			if (i == "font-size") {
				if (cssValue["font-weight"] == "bold") {
					value *= defSizeList.fontBold;
				} else {
					value *= defSizeList.fontNormal;
				}
			}
			$("#" + id).css(i.replace("_", "-"), value);
		}
	}
}

/**
 * @param id
 * @returns
 */
function toggleFold(id) {
	var obj = $("#" + id);
	obj.toggleClass("div-tree-hide");
	obj.toggleClass("div-tree");

	var src = $("#" + id + "_img").attr("src");
	src = src.indexOf("plus") != -1 ? src.replace("plus", "sub") : src.replace(
			"sub", "plus");
	$("#" + id + "_img").attr("src", src);
}

/**
 * @param key
 * @param value
 * @returns
 */
function parseValue(key, value) {
	if (key == "font-size" || key == "width") {

		if (value.indexOf("px") != -1) {
			value = value.replace("px", "");
			console.log(value);
			console.log(value * pdfScale);
			return value * pdfScale;
		}
		else if (value.replace(/[0-9]/g, "").length != 0) {
			return value;
		}
		else {
			return value * pdfScale;
		}
		
	} else if (key == "margin" || key == "padding" || key == "border") {
		var floatArr = [];
		var marginStr = value.split(",");
		for (var i = 0; i < marginStr.length; i++) {
			var m = (marginStr[i] * pdfScale) + "px";
			if (i == 0) {
				floatArr = [ m, m, m, m ];
			} else if (i == 1) {
				floatArr[i] = m;
				floatArr[i + 2] = m;
			} else {
				floatArr[i] = m;
			}
		}
		return floatArr[0] + " " + floatArr[1] + " " + floatArr[2] + " "
				+ floatArr[3];
	}
	return value;
}

/**
 * @param id
 * @returns
 */
function clickElement(id) {
	
	if (id.indexOf("page") != -1) {
		$(".div-sel-page").removeClass("div-sel-page");
		$(".sel-table").removeClass("sel-table");
		$("#" + id).addClass("div-sel-page");
	}

	$("#inneredit_tree").children().remove();

	if (elementList[id] != null) {
	
		var code = elementList[id].tagCode;
		$(".sel-" + code).removeClass("sel-" + code);
		$("#" + id).addClass("sel-" + code);
		
		var cssCode = elementList[id].cssCode;
		var title = elementList[id].text;
	
		var valueList = {"id" : id, "title" : elementList[id].text};
		
		$("#inneredit_tree").append(applyToFormat("TAG_EDIT_TITLE", valueList));

		var brIdx = title.indexOf("\n");
		if (brIdx != -1) {
			$("#now_" + id).text(title.substring(0, brIdx));
		}
		for ( var i in defStyleList[cssCode]) {
	
			if (i == "setCode") {
				continue;
			}
			var value = elementList[id][i];
			value = value == null ? defStyleList[cssCode][i] : value;
			
			valueList.cssName = i;
			valueList.value = value;
			$("#inneredit_tree").append(applyToFormat("TAG_EDIT_CSS", valueList));
		}
		setStyle("now_" + id, elementList[id], defStyleList[cssCode]);
		setStyle("now_" + id, elementList[id], defStyleList["EDIT_DEFALUT"]);

		$("#inneredit_tree").append(applyToFormat("TAG_EDIT_TEXTAREA", valueList));
		$("#inneredit_tree").append(applyToFormat("TAG_EDIT_DELBTN", valueList));
	}
	setBtnActivation();
}

/**
 * @param id
 * @returns
 */
function delElement(id) {
	if (elementList[id].tagCode == "page") {
		$("#" + id).remove();
		$("#box_" + id).remove();	
		var tempIdx = $("#box_page").children().length == 1 ? 0 : 1;
		clickElement($("#box_page").children().eq(tempIdx).attr("id"));
	}
	else {
		$("#" + id).remove();
		$("#box_" + id).remove();	
		clickElement($(".sel-page").attr("id"));
	}
	setBtnActivation();
}

/**
 * @param event
 * @param id
 * @returns
 */
function changeStyle(event, id) {

	var cssCode = elementList[id].cssCode;
	var targetId = event.target.id.replace("edit_", "");

	if (targetId == "colspan" || targetId == "rowspan") {

		var indList = id.replaceAll("table_", "").split("_");
		var row = parseInt(indList[2]);
		var col = parseInt(indList[3]);
		
		var colspan = parseInt($("#" + id).attr("colspan"));
		var rowspan = parseInt($("#" + id).attr("rowspan"));
		colspan = isNaN(colspan) ? 1 : colspan;
		rowspan = isNaN(rowspan) ? 1 : rowspan;

		var preValue = parseInt(event.target.defaultValue);
		var newValue = parseInt(event.target.value);

		preValue = isNaN(preValue) ? 1 : preValue;
		newValue = isNaN(newValue) ? 1 : newValue;
		
		if (!isNaN(newValue)) {
			var sRow, eRow, sCol, eCol;
			var displayVal = "none";

			if (targetId == "colspan") {
				if (preValue > newValue) {
					displayVal = "";
					sCol = col + newValue;
					eCol = col + preValue;
				} else {
					sCol = col + preValue;
					eCol = col + newValue;
				}

				sRow = row;
				eRow = sRow + rowspan;
				$("#" + id).attr("colspan", newValue);
			} else if (targetId == "rowspan") {

				if (preValue > newValue) { // 병합 해제
					displayVal = "";
					sRow = row + newValue;
					eRow = row + preValue;
				} else {
					sRow = row + preValue;
					eRow = row + newValue;
				}

				sCol = col;
				eCol = sCol + colspan;
				$("#" + id).attr("rowspan", newValue);
			}

			for (var i = sRow; i < eRow; i++) {
				for (var j = sCol; j < eCol; j++) {
					$("#table_" + indList[0] + "_" + indList[1] + "_" + i+ "_" + j).css("display", displayVal);
					$("#box_table_" + indList[0] + "_" + indList[1] + "_" + i + "_" + j).css("display", displayVal);
				}
			}
			elementList[id][targetId] = newValue;
			event.target.defaultValue = event.target.value;
		}

	} else {
		var value = elementList[id][targetId];
		value = value == null ? defStyleList[cssCode][targetId] : value;
		var newValue = $("#edit_" + targetId).val();
		if (value != newValue) {
			elementList[id][targetId] = newValue;

			newValue = parseValue(targetId, newValue);
			console.log(newValue);
			if (targetId == "font_size") {
				newValue *= pdfScale;
				if (defStyleList[cssCode]["font-weight"] == "bold") {
					newValue *= defSizeList.fontBold;
				} else {
					newValue *= defSizeList.fontNormal;
				}
			}
			else if (typeof(newValue) == "number") {
				newValue *= pdfScale;
			}

			$("#" + id).css(targetId.replace("_", "-"), newValue);
			if ("padding,font-size,align".indexOf(targetId) == -1) {
				$("#edit_title").css(targetId, newValue);
			}
		}
	}
}

/**
 * @param type
 * @returns
 */
function addSubTag(type) {
	var tagStr = tagStrList[subTagList[type].strCode].tagStr;

	var textArea = document.getElementById('edit_textarea');
	var startCur = textArea.selectionStart;
	var endCur = textArea.selectionEnd;

	var text = textArea.value;

	var forward = text.substring(0, startCur);
	var backward = text.substring(endCur);
	var replace = tagStr.replaceAll("#text#", text.substring(startCur, endCur));
	textArea.value = forward + replace + backward;

	changeText($(".modi_now").attr("id").replace("now_", ""));
	
//	var style = document.createElement('style');
//	style.type = 'text/css';
//	style.innerHTML = '.cssClass { size:20px; }';
//	document.getElementsByTagName('head')[0].appendChild(style);
////	document.getElementById('modi_now').className = 'cssClass';
//	$(".text-bold").addClass('cssClass');
}

/**
 * @param id
 * @returns
 */
function changeText(id) {
	var text = $("#edit_textarea").val();
	var innerTextTag = tagStrList["TAG_INNER_TEXT_01"].tagStr;

	elementList[id].text = text;

	// 개행 있으면 edit title에 첫 줄만 보이도록 > 개행없이 아주 긴 경우 처리 필요
	var brIdx = text.indexOf("\n");
	if (brIdx == -1) {
		$("#edit_title").text(text);
	}
	else {
		$("#edit_title").text(text.substring(0, brIdx));
	}
	
	$("#tree_" + id + "_btn").text(text);

	$("#" + id).children().remove();
	$("#" + id).append(
			innerTextTag.replace("#tagName#", text.replaceAll("\n", "<br>")));
}

/**
 * @param event
 * @returns
 */
function dragEndTextArea(event) {

	var textArea = document.getElementById('edit_textarea');
	var startCur = textArea.selectionStart;
	var endCur = textArea.selectionEnd;

	if (event.type == "mouseup" && startCur != endCur) {
		$("#css_modal").css("display", "grid");
		$("#css_modal").css("top", event.pageY);
		$("#css_modal").css("left", event.pageX);
	} else {
		$("#css_modal").css("display", "none");
	}
}

/**
 * @param id
 * @returns
 */
function dragStart(id) {
	$("#" + id).addClass("drag");
	$("#del_guide").addClass("div-del-guide-act1");
}

/**
 * @param event
 * @param id
 * @returns
 */
function dragEnd(event, id) {

	var dragId = $(".drag").attr("id");
	var docRect = document.getElementById($(".div-sel-page").attr("id"))
			.getBoundingClientRect();
	
	if (docRect.bottom + docRect.top < event.screenY) { // 삭제 이벤트시
		delElement(dragId);
	}
	else { // 순서 변경
		var selId = $(".div-sel-page").attr("id");
		var selPage = document.getElementById($(".div-sel-page").attr("id"));
		var dragElement = document.getElementById($(".drag").attr("id"));

		var selPage2 = document.getElementById(selId + "_div");
		var dragElement2 = document.getElementById("box_" + dragId);

		var underDragId = $(".border-underdrag").attr("id");
		var overDragId = $(".border-overdrag").attr("id");

		if (underDragId != null) {
			var underDrag = document.getElementById(underDragId);
			var underDrag2 = document.getElementById("box_" + underDragId);

			if (underDrag.nextSibling == null) {
				selPage.append(dragElement);
				selPage2.append(dragElement2);
			} else {
				selPage.insertBefore(dragElement, underDrag.nextSibling);
				selPage2.insertBefore(dragElement2, underDrag2.nextSibling);
			}
		} else if (overDragId != null) {
			var overDrag = document.getElementById(overDragId);
			var overDrag2 = document.getElementById("box_" + overDragId);
			selPage.insertBefore(dragElement, overDrag);
			selPage2.insertBefore(dragElement2, overDrag2);
		}
	}
	
	$("#del_guide").removeClass("div-del-guide-act2");
	$("#del_guide").removeClass("div-del-guide-act1");
	$(".drag").toggleClass("drag", "");
	$("#" + underDragId).removeClass("border-underdrag");
	$("#" + overDragId).removeClass("border-overdrag");
}

/**
 * @param id
 * @returns
 */
function dragEnter(id) {
	$(".border-underdrag").removeClass("border-underdrag");
	$(".border-overdrag").removeClass("border-overdrag");
	$("#" + id).addClass("border-underdrag");
	
	$("#del_guide").addClass("div-del-guide-act1");
	$("#del_guide").removeClass("div-del-guide-act2");
}

/**
 * @param event
 * @param id
 * @returns
 */
function dragLeave(event, id) {
	
	if (event.layerY > document.getElementById($(".div-sel-page").attr("id"))
	.getBoundingClientRect().bottom) {
		$("#del_guide").addClass("div-del-guide-act2");
		$("#del_guide").removeClass("div-del-guide-act1");
		$(".border-underdrag").removeClass("border-underdrag");
	}
	else {
		var topY = document.getElementById($(".div-sel-page").attr("id")).firstChild
		.getBoundingClientRect().top;
		
		if (elementList[id].tagCode != "page" && event.layerY > 0 && topY > event.layerY) {
			$(".border-underdrag").removeClass("border-underdrag");
			$(".border-overdrag").removeClass("border-overdrag");
			$("#" + id).addClass("border-overdrag");
		}
	}
	
}

/**
 * @param tagInfo
 * @param keyList
 * @returns
 */
function makeValueList(tagInfo, keyList) {
	var valueList = new Array();
	for ( var i in keyList) {
		valueList[keyList[i]] = tagInfo[keyList[i]];
	}
	return valueList;
}

/**
 * @param tagId
 * @param tagValue
 * @returns
 */
function applyToFormat(tagId, tagValue) {
	var tempStr = tagStrList[tagId].tagStr;
	tempStr = tempStr.replaceAll("#innerText#",
			tagStrList["TAG_INNER_TEXT_01"].tagStr);
	tempStr = tempStr.replaceAll("#dragStr#", tagStrList["CSS_DRAG_01"].tagStr);

	for ( var i in tagValue) {
		tempStr = tempStr.replaceAll("#" + i + "#", tagValue[i]);
	}
	return tempStr;
}

//다운로드
/**
 * @param id
 * @returns
 */
function download(id) {
	var pdfBody = makeTxt('box_page');

	console.log(pdfBody);
	$.ajax({
		url : "/downloadFromMaker.do",
		type : "GET",
		data : {"pdfBody" : pdfBody},
		contentType : "application/text; charset=UTF-8",
		success : function(result) {
		},
		error : function(request, status, error) {
		}
	});
	
}

/**
 * @param id
 * @returns
 */
function makeTxt(id) {

	var obj = document.getElementById(id).children;
	var str = "";
	
	for (var i = 0; i < obj.length; i++) {
		var cId = obj.item(i).id;
		if (cId == "page_empty") {
			continue;
		}
		else if (cId.length == 0) {
			var rtnText = obj.item(i).innerText.replaceAll("\n", "<br>");
			str += (rtnText == null ? "" : rtnText);
		}
		else {
			var cType = elementList[cId].tagCode;
			if (cType != "page" && $("#" + cId).css("display") == "none") {
				continue;
			}
			str += "\n";
			str += makeTagStr(cId, "sTag");
			
			if (obj.item(i).children.length > 0) {
				str += makeTxt(cId);
			}
			str += makeTagStr(cId, "eTag");

		}
	}
	return str;
}

/**
 * @param id
 * @param flag
 * @returns
 */
function makeTagStr(id, flag) {
	var element = elementList[id];
	var str = "";
	if (element != null) {
		if (flag == "sTag") {
			str = tagList[elementList[id].tagCode].tagType;
			for (var name in defStyleList[element.cssCode]) {
				
				if (name == "setCode") {
					continue;
				}
				
				var value = elementList[id][name];
				value = value == null || value.length == 0 ? defStyleList[element.cssCode][name] : value;
				if (value != null && value.length != 0){
					str += " ";
					str += name + "=\"";
					str += value + "\"";
				}
			}
		}
		else if ("eTag") {
			str = "/"
				+ tagList[elementList[id].tagCode].tagType;
		}
	}
	str = "<" + str + ">";
	return str;
}

/**
 * @returns
 */
function setBtnActivation() {

	for ( var i in tagList) {
		var selId = ".sel-" + tagList[i].parentCode.toLowerCase();
		if (tagList[i].parentCode == "ROOT" || $(selId).length != 0) {
			$("#btn_add_" + i).attr("disabled", false);
			$("#btn_add_" + i).removeClass("btn-add-disable");
			$("#btn_add_" + i).addClass("btn-add-element");
		}
		else {
			$("#btn_add_" + i).attr("disabled", true);
			$("#btn_add_" + i).addClass("btn-add-disable");
			$("#btn_add_" + i).removeClass("btn-add-element");
		}
	}
}

function trim(str) {
	return str.replace(/^\s+|\s+$/g, "");
}
