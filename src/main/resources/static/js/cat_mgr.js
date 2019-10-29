
function postCatData(category) {
    var fileData = new FormData();
    var data =  ($("#" + category).serializeJSON())
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
            data.logo = response;
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
                    alert("category created!\ncategory ID = " + res.category_id);
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