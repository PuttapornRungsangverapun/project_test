<?php 
include("db_con.php");
header('Content-type:application/json');
$user_name = $_REQUEST["username"];
$user_pass = sha1($_REQUEST["password"]);

//$mysql_qry = "select user_id,user_username,user_password from users where user_username = '$user_name'";
$mysql_qry ="select users.user_id,users.user_username username,users.user_password,tokens.token_body token
from users left join tokens on users.user_id = tokens.user_id
where users.user_username='$user_name'";


/*$stmt=mysqli_prepare($conn, "select users.user_id,users.user_username username,users.user_password,tokens.token_body token
from users left join tokens on users.user_id = tokens.user_id
where users.user_username=?");
mysqli_stmt_bind_param($stmt,"s",$user_name);
mysqli_stmt_execute($stmt);
mysqli_stmt_bind_result($stmt, $id,$username,$password,$token);
mysqli_stmt_fetch($stmt);*/
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
//echo $id . ":" . $username . ":" . $password . ":" . $tokend;

if($row){
	
	if($row['user_password']==($user_pass)){
		
		$ret['status']="success";
		$ret['userid']=intval($row['user_id']);	
		$ret['message']="Login success";
		$ret['username']=$row['username'];
		
		$mysql_qry1 = "select user_id,token_body from tokens where user_id = '".$row['user_id']."'";
		$result1 = mysqli_query($conn ,$mysql_qry1);
		$rowtoken=mysqli_fetch_array($result1);

		if(!$rowtoken){
			$token=bin2hex(openssl_random_pseudo_bytes(32));
			$mysql_qry2 = "insert into tokens(token_body,user_id) values('$token','".$row['user_id']."')";
			$result2 = mysqli_query($conn ,$mysql_qry2);
			$ret['token']=$token;
		}
		else{
			$ret['token']=$rowtoken['token_body'];
		}
	}
	else{
		$ret['status']="fail1";	
		$ret['message']="Login fail";
	}
	
}else{
	$ret['status']="fail2";	
	$ret['message']="Login fail";
}

echo json_encode($ret);	
?>
