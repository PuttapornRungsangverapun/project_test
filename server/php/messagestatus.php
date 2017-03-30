<?php
include("db_con.php");
include("token.php");
header('Content-type:application/json');
//$user_id = $_REQUEST["userid"];
$user_id_friend = $_REQUEST["friendid"];
$last_message_id = $_REQUEST["lastmessageid"];


$mysql_qry = "SELECT  messages.message_id,
LOWER(DATE_FORMAT(message_creation_date,'%l:%i %p')) time,
messages.message_sender_id,
messages.message_type,
messages.message_status,
messages_texts.text_body,
messages_files.file_filename,
messages_maps.map_latitude,
messages_maps.map_longitude
FROM    messages
left JOIN messages_files
on messages.message_id = messages_files.message_id
left JOIN messages_texts
on messages.message_id =  messages_texts.message_id
left JOIN messages_maps
on messages.message_id =  messages_maps.message_id
where ((message_sender_id=? and message_receiver_id=?)
or (message_sender_id=? and message_receiver_id=?)) and (messages.message_id > ?)
and (messages_files.message_id is not null
or messages_texts.message_id is not null
or messages_maps.message_id is not null)
order by  messages.message_id asc";
//echo $mysql_qry;
// $result = mysqli_query($conn ,$mysql_qry);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'sssss',$user_id,$user_id_friend,$user_id_friend,$user_id,$last_message_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);

$message=array();
$ret = array();
while($row=mysqli_fetch_assoc($result)){
    if($row){
        $message[]=$row;
    }
    
}
$ret['status']="success";
$ret['message']=$message;
echo json_encode($ret);


for($i=0;$i<sizeof($message);$i++){
    $row=$message[$i];
    $message_status=$row['message_status'];
    if($user_id==$row['message_sender_id']){
        if($row['message_status']==1){
            $message_status=2;
        }
        else if($row['message_status']==3){
            $message_status=4;
        }
    }
    else{if($row['message_status']==1){
        $message_status=3;
    }
    else if($row['message_status']==2){
        $message_status=4;
    }}
    $mysql_qry = "update messages set message_status =? where message_id = ?";
    // $result = mysqli_query($conn ,$mysql_qry);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$message_status,$row['message_id']);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    
}

?>