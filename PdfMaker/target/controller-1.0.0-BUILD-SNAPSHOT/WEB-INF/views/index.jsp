<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
   <meta charset="UTF-8">
   <title>PDF 생성기</title>
   <script type="text/javascript" src="/resources/js/jquery-3.5.1.min.js"></script>
   <script type="text/javascript" src="/resources/js/pdf.js" charset="UTF-8"></script>
   <link rel="stylesheet" href="/resources/css/pdf.css?ver=3">
   <link rel="shortcut icon" href="/resources/img/pdf.png" type="image/x-icon">
</head>
<body>
   <div id="css_modal" style="display:none; padding:5px; border-radius:0.5em; position:absolute; background-color:#6e93c1;"></div>
   <div align="center">
      <div class="header">PDF 생성기</div>
	  <div align="right" onclick="download()" style="margin:10px;"><button>다운로드</button></div>
      <div>
      <div class="div-preview">
         <div style="display:inline-flex; align-items:center;">
             <div style="width: 416.693px; height: 589.323px; position:absolute; pointer-events: none;">
               <div id="del_guide" class="div-del-guide" style="align-items:end;">▼ ▼ ▼ 삭제 ▼ ▼ ▼</div>
             </div>
            <div id="box_page">
              <div id="page_empty" class="div-sel-page div-page">
                <div class="box-empty">EMPTY</div>
              </div>
            </div>
         </div>
         <div class="text-bold">PAGE 1</div>

         <div class="div-add-element">
            <div class="text-bold">요소추가</div>
            <div id="box_addbtn" style="margin:10px;"></div>
         </div>
      </div>

      <div style="background-color:#3F74A3; padding:30px; width:100%; box-sizing: border-box;">
         <div style="display:inline-flex; align-items:center;">
           <div id="box_tree" align="left" class="box-tree"><div id="innerbox_tree"></div></div>
           <div id="box_edit" align="left" class="box-tree"><div id="inneredit_tree"></div></div>
         </div>
    </div>
  </div>
   </div>
   <div>Icons made by <a href="https://www.flaticon.local/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.local/" title="Flaticon">www.flaticon.local</a></div>
</body>
</html>