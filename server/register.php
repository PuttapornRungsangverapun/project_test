<?php

include("db_con.php");
header('Content-type:application/json');
$user_name = $_REQUEST["username"];
$user_pass = sha1($_REQUEST["password"]);
$user_email = $_REQUEST["email"];
$user_publickey = $_REQUEST["publickey"];

// Remove all illegal characters from email
$email = filter_var($user_email, FILTER_SANITIZE_EMAIL);

$result = mysqli_query($conn,"select user_username,user_email from users where user_username='".$user_name."' or user_email='".$user_email."'");
$row=mysqli_fetch_array($result);
$ret=array();

        if(!$row){
			
                $mysql_qry = "insert into users(user_username,user_password,user_status_id ,user_email,user_publickey) values ('$user_name','$user_pass','1','$user_email','$user_publickey')";
                $result = mysqli_query($conn ,$mysql_qry);
				
				$userid = mysqli_insert_id($conn);
				$token=bin2hex(openssl_random_pseudo_bytes(32));
				$mysql_qry2 = "insert into tokens(token_body,user_id) values('$token','".$userid."')";
				$result2 = mysqli_query($conn ,$mysql_qry2);
			
				$ret['token']=$token;
				$ret['username']=$user_name;
				$ret['status']="success";
				$ret['message']="Register success";
				$ret['userid']=$userid;
			
			
            }
        else{
				$ret['status']="fail2";
				$ret['message']="Register fail";
            }

echo json_encode($ret);

?>

