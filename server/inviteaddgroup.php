<?php

include("db_con.php");
include('token.php');
header('Content-type:application/json');
$group_id = $_REQUEST["groupid"];
$friend_id = $_REQUEST["friendid"];

$result = mysqli_query($conn,"SELECT users.user_id FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id WHERE groups_users.user_id='$friend_id' and groups_users.group_id='$group_id'");
$row=mysqli_fetch_array($result);
$ret=array();

        if(!$row){
			
				$mysql_qry2 = "insert into groups_users(group_id,user_id,addby_userid) values ('$group_id','$friend_id','$user_id')";
                $result1 = mysqli_query($conn ,$mysql_qry2);
				
				
				$ret['status']="success";
				$ret['message']="Invite Friend success";
			
			
            }
        else{
				$ret['status']="fail";
				$ret['message']="Invite friend fail";
            }

echo json_encode($ret);

?>

