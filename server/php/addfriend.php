<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');

//$user_id = $_REQUEST["userid"];
$user_name_friend = $_REQUEST["username"];
$mysql_qry = "select user_id,user_username from users where user_username = '$user_name_friend' and user_status_id = '1'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
$ret = array();

	if($row){
		$frined_id=$row['user_id'];
		
		if(!($frined_id==$user_id)){
			$mysql_qry = "insert into friends(user_id,friend_id) values ('$user_id','$frined_id')";
			mysqli_query($conn ,$mysql_qry);
			$ret['status']="success";	
			$ret['message']="Success";
		}
		else{
			$ret['status']="error";	
			$ret['message']="Error";
		}
	}
	else{
		$ret['status']="error";	
		$ret['message']=$user_name_friend." does not exist";
		//$ret['sql']=$mysql_qry;
	}
echo json_encode($ret);	
?>
