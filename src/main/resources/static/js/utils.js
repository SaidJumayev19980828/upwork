
function openPage(URL){
	$.ajax({
	            url: URL,
	            type: 'GET',
	            dataType: 'html',
	            headers: {
	                "User-Token": token	                
	            },
	            contentType: 'text/html; charset=utf-8',
	            success: function (result) {
	                $(window.document.body).html(result);
	            },
	            error: function (error) {
	                alert("Failed to open Page!")
	            }
	        });
   
}