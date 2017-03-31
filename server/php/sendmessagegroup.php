<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');
//$user_id_current = $_REQUEST["userid"];
$group_id = $_REQUEST["groupid"];
$user_message =$_REQUEST["message"];
$message_type =$_REQUEST["type"];
$message_filename = $_REQUEST["filename"];
$map_latitude = $_REQUEST["latitude"];
$map_longitude=$_REQUEST["longitude"];
if($message_type=='file'){
    $md5=$_REQUEST["md5"];
}if($message_type=='authen'){
    $target_id = $_REQUEST["targetid"];
}

$mysql_qry = "select groups.group_id from groups where groups.group_id =?";
// $result = mysqli_query($conn ,$mysql_qry);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$group_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);
if(!$row){
    $ret['status']="error";
    $ret['message']="Group does not exist";
    echo json_encode($ret);
    exit();
}

if($message_type != 'authen'){
    $mysql_qry = "select groups_users.user_id from groups_users where groups_users.group_id=? and groups_users.user_id=?";
    // $result = mysqli_query($conn ,$mysql_qry);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$group_id,$user_id);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    $row=mysqli_fetch_array($result);
    if(!$row){
        $ret['status']="error";
        $ret['message']="User isn't group";
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
/*if(($user_message !=null && strlen($user_message)<25)||( $map_longitude!=null && strlen($map_longitude)<25)||($map_longitude!=null && strlen($map_latitude)<25)){
$ret['status']="error";
$ret['message']="Send fail2," . $user_message . ',' . $map_latitude . ',' . $map_longitude;
echo json_encode($ret);
exit();
}*/

$mysql_qry = "insert into groups_messages(group_message_sender_id,group_id,group_message_status,group_message_type) values (?,?,'1',?)";
// $row=mysqli_query($conn ,$mysql_qry1);
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'sss',$user_id,$group_id,$message_type);
$row=mysqli_stmt_execute($result);
$messagesid = mysqli_insert_id($conn);

//$mysql_qryid = "select max(message_id) from messages";
//$resultid=mysqli_query($conn ,$mysql_qryid);
//$rowid=mysqli_fetch_array($resultid);

if($message_type=='text'){
    $mysql_qry = "insert into groups_messages_texts(text_body,group_message_id) values (?,?)";
    // $row2=mysqli_query($conn ,$mysql_qry2);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$user_message,$messagesid);
    mysqli_stmt_execute($result);
}
else if($message_type=='file'){
    $mysql_qry = "insert into groups_messages_files(file_body,group_message_id,file_filename) values (?,?,?)";
    // $row3=mysqli_query($conn ,$mysql_qry3);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$user_message,$messagesid,$message_filename);
    mysqli_stmt_execute($result);
}
else if($message_type=='map'){
    $mysql_qry = "insert into groups_messages_maps(map_latitude,map_longitude,group_message_id) values (?,?,?)";
    // $row3=mysqli_query($conn ,$mysql_qry3);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$map_latitude,$map_longitude,$messagesid);
    mysqli_stmt_execute($result);
}

else if($message_type=='authen'){
    $mysql_qry = "insert into groups_messages_texts(text_body,group_message_id,target_userid) values (?,?,?)";
    // $row4=mysqli_query($conn ,$mysql_qry4);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$user_message,$messagesid,$target_id);
    mysqli_stmt_execute($result);
}


if($row){
    $ret['status']="success";
    $ret['message']="Success";
    if($message_type != 'authen'){
        include('sendnotification.php');
        $mysql_qry = "SELECT groups.group_name groupname,groups_users.user_id FROM groups LEFT JOIN groups_users ON groups.group_id=groups_users.group_id WHERE groups.group_id=? and groups_users.user_id<>?";
        // $result=mysqli_query($conn ,$mysql_qry5);
        
        $result = mysqli_prepare($conn ,$mysql_qry);
        mysqli_stmt_bind_param($result,'ss',$group_id,$user_id);
        mysqli_stmt_execute($result);
        $result= mysqli_stmt_get_result($result);
        
        $mysql_qry = "select user_username from users where user_id=?";
        // $result2=mysqli_query($conn ,$mysql_qry6);
        
        $result2 = mysqli_prepare($conn ,$mysql_qry);
        mysqli_stmt_bind_param($result2,'s',$user_id);
        mysqli_stmt_execute($result2);
        $result2= mysqli_stmt_get_result($result2);
        
        $row6=mysqli_fetch_array($result2);
        
        
        while($row5=mysqli_fetch_assoc($result)){
            sendnoti($row5['user_id'],'G'.$group_id,$row6['user_username'].';'.$row5['groupname'],"New Message");
        }
        
    }
}
else{
    $ret['status']="error2";
    $ret['message']="Send fail";
}
echo json_encode($ret);

?>