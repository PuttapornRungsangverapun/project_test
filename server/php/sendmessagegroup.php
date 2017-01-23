<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');
//$user_id_current = $_REQUEST["userid"];
$group_id = $_REQUEST["groupid"];
$target_id = $_REQUEST["targetid"];
$user_message =$_REQUEST["message"];
$message_type =$_REQUEST["type"];
$message_filename = $_REQUEST["filename"];
$map_latitude = $_REQUEST["latitude"];
$map_longitude=$_REQUEST["longitude"];

$mysql_qry = "select groups.group_id from groups where groups.group_id ='$group_id'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
if(!$row){
$ret['status']="error";	
$ret['message']="Group does not exist";
echo json_encode($ret);	
exit();
}

if($message_type != 'authen'){
	$mysql_qry = "select groups_users.user_id from groups_users where groups_users.group_id='$group_id' and groups_users.user_id='$user_id'";
	$result = mysqli_query($conn ,$mysql_qry);
	$row=mysqli_fetch_array($result);
	if(!$row){
	$ret['status']="error";	
	$ret['message']="User isn't group"; 
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
	
$mysql_qry1 = "insert into groups_messages(group_message_sender_id,group_id,group_message_status,group_message_type) values ('$user_id','$group_id','1','$message_type')";
$row=mysqli_query($conn ,$mysql_qry1);
$messagesid = mysqli_insert_id($conn);

//$mysql_qryid = "select max(message_id) from messages";
//$resultid=mysqli_query($conn ,$mysql_qryid);
//$rowid=mysqli_fetch_array($resultid);

if($message_type=='text'){
$mysql_qry2 = "insert into groups_messages_texts(text_body,group_message_id) values ('$user_message','$messagesid')";
$row2=mysqli_query($conn ,$mysql_qry2);
}
else if($message_type=='file'){
$mysql_qry3 = "insert into groups_messages_files(file_body,group_message_id,file_filename) values ('$user_message','$messagesid','$message_filename')";
$row3=mysqli_query($conn ,$mysql_qry3);
}
else if($message_type=='map'){
$mysql_qry3 = "insert into groups_messages_maps(map_latitude,map_longitude,group_message_id) values ('$map_latitude','$map_longitude','$messagesid')";
$row3=mysqli_query($conn ,$mysql_qry3);
}

else if($message_type=='authen'){
$mysql_qry4 = "insert into groups_messages_texts(text_body,group_message_id,target_userid) values ('$user_message','$messagesid','$target_id')";
$row4=mysqli_query($conn ,$mysql_qry4);
}


if($row){
	$ret['status']="success";	
	$ret['message']="Success";
	if($message_type != 'authen'){
		include('sendnotification.php');
		$mysql_qry5 = "SELECT groups.group_name groupname,groups_users.user_id FROM groups LEFT JOIN groups_users ON groups.group_id=groups_users.group_id WHERE groups.group_id='$group_id' and groups_users.user_id<>'$user_id'";
		$result=mysqli_query($conn ,$mysql_qry5);
		
		$mysql_qry6 = "select user_username from users where user_id=$user_id";
		$result2=mysqli_query($conn ,$mysql_qry6);
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