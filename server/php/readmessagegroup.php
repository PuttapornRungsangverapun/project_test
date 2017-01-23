<?php 
include("db_con.php");
include("token.php");
header('Content-type:application/json');
//$user_id = $_REQUEST["userid"];
$group_id = $_REQUEST["groupid"];
$last_message_id = $_REQUEST["lastmessageid"];


$mysql_qry = "SELECT  groups_messages.group_message_id,
LOWER(DATE_FORMAT(group_message_creation_date,'%l:%i %p')) time,
groups_messages.group_message_sender_id,
groups_messages.group_id,
groups_messages.group_message_type,
groups_messages.group_message_status,
groups_messages_texts.text_body,
groups_messages_files.file_filename,
groups_messages_maps.map_latitude,
groups_messages_maps.map_longitude,
groups_messages_texts.target_userid,
users.user_username
FROM    groups_messages
      left JOIN groups_messages_files 
	  on groups_messages.group_message_id = groups_messages_files.group_message_id
       left JOIN groups_messages_texts
        on groups_messages.group_message_id =  groups_messages_texts.group_message_id
		left JOIN groups_messages_maps
        on groups_messages.group_message_id =  groups_messages_maps.group_message_id 
        LEFT JOIN users 
        ON users.user_id=groups_messages.group_message_sender_id
		where  (groups_messages.group_id='$group_id') and (groups_messages.group_message_id > $last_message_id) 
		and (groups_messages_files.group_message_id is not null
		or groups_messages_texts.group_message_id is not null 
		or groups_messages_maps.group_message_id is not null)
		order by  groups_messages.group_message_id asc";
//echo $mysql_qry;
$result = mysqli_query($conn ,$mysql_qry);
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


/*for($i=0;$i<sizeof($message);$i++){
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
$mysql_qry = "update messages set message_status ='$message_status' where message_id = '".$row['message_id']."'";
$result = mysqli_query($conn ,$mysql_qry);
}*/

?>
