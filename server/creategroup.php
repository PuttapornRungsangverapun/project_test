<?php

include("db_con.php");
include('token.php');
header('Content-type:application/json');
$group_name = $_REQUEST["groupname"];
$friend_id = $_REQUEST["frienid"];

$result = mysqli_query($conn,"SELECT groups.group_name FROM groups WHERE groups.group_name = '".$group_name."'");
$row=mysqli_fetch_array($result);
$ret=array();

        if(!$row){
			
                $mysql_qry = "insert into groups(group_name) values ('$group_name')";
                $result = mysqli_query($conn ,$mysql_qry);
				
				$groupid = mysqli_insert_id($conn);
				
				$mysql_qry2 = "insert into groups_users(group_id,user_id) values ('$groupid','$user_id')";
                $result1 = mysqli_query($conn ,$mysql_qry2);
				
				$mysql_qry3 = "insert into groups_users(group_id,user_id) values ('$groupid','$friend_id')";
                $result2 = mysqli_query($conn ,$mysql_qry3);
				
				$ret['status']="success";
				$ret['groupid']=$groupid ;
				$ret['message']="Create group success";
				
			
			
            }
		else if($row){
			
			$result2 = mysqli_query($conn,"SELECT groups.group_name FROM groups WHERE groups.group_name = '".$group_name."'");
			$rowid=mysqli_fetch_array($result);
			if(!$rowid){
				$result = mysqli_query($conn,"SELECT groups.group_id gid FROM groups WHERE groups.group_name = '".$group_name."'");
				$row2=mysqli_fetch_array($result);
				$groupid=$row2['gid'];
				
				$mysql_qry2 = "insert into groups_users(group_id,user_id) values ('$groupid','$user_id')";
                $result1 = mysqli_query($conn ,$mysql_qry2);
				
				$mysql_qry3 = "insert into groups_users(group_id,user_id) values ('$groupid','$friend_id')";
                $result2 = mysqli_query($conn ,$mysql_qry3);
			$ret['status']="success";
			}
			
			}
        else{
				$ret['status']="fail";
				$ret['message']="Create group fail";
            }

echo json_encode($ret);

?>

