<?php
include('db_con.php');
include('token.php');
header('Content-type:application/json');
//$user_id_current = $_REQUEST["userid"];
$user_id_friend = $_REQUEST["friendid"];
$user_message =$_REQUEST["message"];
$message_type =$_REQUEST["type"];
$message_filename = $_REQUEST["filename"];
$map_latitude = $_REQUEST["latitude"];
$map_longitude=$_REQUEST["longitude"];
if($message_type=='authen'){
$target_id=$_REQUEST["targetid"];
}
if($message_type=='file'){
    $md5=$_REQUEST["md5"];
}
$mysql_qry = "select user_id from users where user_id = ? and user_status_id = '1'";
//$result = mysqli_query($conn ,$mysql_qry);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$user_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);
$ret = array();
if(!$row){
    $ret['status']="error";
    $ret['message']="user does not exist";
    echo json_encode($ret);
    exit();
}

$mysql_qry = "select user_id from users where user_id = ? and user_status_id = '1'";
//$result = mysqli_query($conn ,$mysql_qry);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$user_id_friend);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);
if(!$row){
    $ret['status']="error";
    $ret['message']="user does not exist";
    echo json_encode($ret);
    exit();
}

if($message_type != 'authen'){
    $mysql_qry = "select user_id,friend_id from friends where (user_id=? and friend_id=?) or (user_id=? and friend_id=?)";
    //$result = mysqli_query($conn ,$mysql_qry);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ssss',$user_id,$user_id_friend,$user_id_friend,$user_id);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    $row=mysqli_num_rows($result);
    if($row!=2){
        $ret['status']="error";
        $ret['message']="User isn't friend";
        echo json_encode($ret);
        exit();
    }
}
if(($message_type != 'authen')&&($message_type == 'file')){
    $mysql_qry = "select status from virus_hash where md5 = ? and status='A'";
    // $result = mysqli_query($conn ,$mysql_qry);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'s',$md5);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    $row=mysqli_fetch_array($result);
    if($row){
        $ret['status']="error";
        $ret['message']="Virus Definition";
        echo json_encode($ret);
        exit();
    }
}

if(($user_message !=null && strlen($user_message)<25)||( $map_longitude!=null && strlen($map_longitude)<25)||($map_longitude!=null && strlen($map_latitude)<25)){
    $ret['status']="error";
    $ret['message']="Send fail2," . $user_message . ',' . $map_latitude . ',' . $map_longitude;
    echo json_encode($ret);
    exit();
}

$mysql_qry = "insert into messages(message_sender_id,message_receiver_id,message_status,message_type) values (?,?,'1',?)";
// $row=mysqli_query($conn ,$mysql_qry1);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'sss',$user_id,$user_id_friend,$message_type);
$row=mysqli_stmt_execute($result);
// $row= mysqli_stmt_get_result($result);
$messagesid = mysqli_insert_id($conn);

//$mysql_qryid = "select max(message_id) from messages";
//$resultid=mysqli_query($conn ,$mysql_qryid);
//$rowid=mysqli_fetch_array($resultid);

if($message_type=='text'){
    $mysql_qry = "insert into messages_texts(text_body  ,message_id) values (?,?)";
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$user_message,$messagesid);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
}
else if($message_type=='file'){
    $mysql_qry = "insert into messages_files(file_body,message_id,file_filename) values (?,?,?)";
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$user_message,$messagesid,$message_filename);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
}
else if($message_type=='map'){
    $mysql_qry = "insert into messages_maps(map_latitude,map_longitude,message_id) values (?,?,?)";
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$map_latitude,$map_longitude,$messagesid);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
}
else if($message_type=='authen'){
    $mysql_qry = "insert into messages_texts(text_body,message_id,targer_userid) values (?,?,?)";
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$user_message,$messagesid,$target_id);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
}


if($row){
    $ret['status']="success";
    $ret['message']="Success";
    if($message_type != 'authen'){
        include('sendnotification.php');
        $mysql_qry = "select user_username from users where user_id=?";
        // $result=mysqli_query($conn ,$mysql_qry5);
        $result = mysqli_prepare($conn ,$mysql_qry);
        mysqli_stmt_bind_param($result,'s',$user_id);
        mysqli_stmt_execute($result);
        $result= mysqli_stmt_get_result($result);
        $row5=mysqli_fetch_array($result);
        sendnoti($user_id_friend,'U'.$user_id,$row5['user_username'],"New Message");
    }
}
else{
    $ret['status']="error2";
    $ret['message']="Send fail";
}
echo json_encode($ret);

?>