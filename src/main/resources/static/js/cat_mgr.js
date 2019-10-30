
function postCatData(category) {
    if($("#category_selector").val() != "update")
        $("#cat_id").val('');

    var fileData = new FormData();
    var data =  $("#" + category).serializeJSON();

    data.operation = $("#category_selector").val();

    data = removeEmpty(data);
    fileData.append('file', document.getElementById('logo').files[0]);
    $.ajax({
        type: 'POST',
        url: "/files",
        headers: {
            "User-ID": 68,
            "User-Token": token,
            'Content-Type': undefined
        },
        cache: false,
        processData: false,
        contentType: false,
        enctype: 'multipart/form-data',
        data: fileData,
        success: function (response) {
        },
        complete: function (response){
            if(response.responseText)
                data.logo = response.responseText;
            console.log(JSON.stringify(data))
            $.ajax({
                type: 'POST',
                url: "/admin/category",
                headers: {
                    "User-ID": 68,
                    "User-Token": token,
                    "Content-Type": "application/json; charset=utf-8"
                },
                data: JSON.stringify(data),
                success: function (res) {
                    alert("category "+$("#category_selector").val()+"d\ncategory ID = " + res.category_id);
                },
                error: function (xhr) {
                    alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
                }
            });
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
        }
    });

     //End of Ajax
}

function  removeEmpty(obj) {
    for (var propName in obj)
        if (obj[propName] == "")
            delete obj[propName];

    return obj;
}