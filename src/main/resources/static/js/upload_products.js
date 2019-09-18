function upload(){

    $.ajax({
        type : 'POST',       
        url : "upload/productlist",        
        headers : {
            "User-Token" : token
        },
        contentType : 'application/x-www-form-urlencoded',
        //Add form data
        data : {keyName : dataValue},
        success : function(response) {
            console.log(response);
        },
        error : function(xhr, status, error) {
            var err = eval("(" + xhr.responseText + ")");
            console.log(err);                   
        }
    }); //End of Ajax

}