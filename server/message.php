<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');
//$user_id_current = $_REQUEST["userid"];
$user_id_friend = $_REQUEST["friendid"];
$user_message =$_REQUEST["message"];
$message_type =$_REQUEST["type"];
$message_filename = $_REQUEST["filename"];
$map_latitude = $_REQUEST["latitude"];
$map_longitude=$_REQUEST["longitude"];

$mysql_qry = "select user_id from users where user_id = '$user_id' and user_status_id = '1'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
$ret = array();
if(!$row){
$ret['status']="error";	
$ret['message']="user does not exist";
echo json_encode($ret);	
exit();
}

$mysql_qry = "select user_id from users where user_id = '$user_id_friend' and user_status_id = '1'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
if(!$row){
$ret['status']="error";	
$ret['message']="user does not exist";
echo json_encode($ret);	
exit();
}

if($message_type != 'authen'){
	$mysql_qry = "select user_id,friend_id from friends where (user_id='$user_id' and friend_id='$user_id_friend') or (user_id='$user_id_friend' and friend_id='$user_id')";
	$result = mysqli_query($conn ,$mysql_qry);
	$row=mysqli_num_rows($result);
	if($row!=2){
	$ret['status']="error";	
	$ret['message']="User isn't friend"; 
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
	
$mysql_qry1 = "insert into messages(message_sender_id,message_receiver_id,message_status,message_type) values ('$user_id','$user_id_friend','1','$message_type')";
$row=mysqli_query($conn ,$mysql_qry1);
$messagesid = mysqli_insert_id($conn);

//$mysql_qryid = "select max(message_id) from messages";
//$resultid=mysqli_query($conn ,$mysql_qryid);
//$rowid=mysqli_fetch_array($resultid);

if($message_type=='text'){
$mysql_qry2 = "insert into messages_texts(text_body  ,message_id) values ('$user_message','$messagesid ')";
$row2=mysqli_query($conn ,$mysql_qry2);
}
else if($message_type=='file'){
$mysql_qry3 = "insert into messages_files(file_body,message_id,file_filename) values ('$user_message','$messagesid','$message_filename')";
$row3=mysqli_query($conn ,$mysql_qry3);
}
else if($message_type=='map'){
$mysql_qry3 = "insert into messages_maps(map_latitude,map_longitude,message_id) values ('$map_latitude','$map_longitude','$messagesid')";
$row3=mysqli_query($conn ,$mysql_qry3);
}
else if($message_type=='authen'){
$mysql_qry4 = "insert into messages_texts(text_body  ,message_id) values ('$user_message','$messagesid ')";
$row4=mysqli_query($conn ,$mysql_qry4);
}


if($row){
	$ret['status']="success";	
	$ret['message']="Success";
	if($message_type != 'authen'){
		include('sendnotification.php');
		$mysql_qry5 = "select user_username from users where user_id=$user_id";
		$result=mysqli_query($conn ,$mysql_qry5);
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