<?php

include("db_con.php");
header('Content-type:application/json');
$user_name = $_REQUEST["username"];
$user_pass = sha1($_REQUEST["password"]);
$user_email = $_REQUEST["email"];

// Remove all illegal characters from email
$email = filter_var($user_email, FILTER_SANITIZE_EMAIL);

// $result = mysqli_query($conn,"select user_username,user_email from users where user_username='".$user_name."' or user_email='".$user_email."'");
$mysql_qry="select user_username,user_email from users where user_username=? or user_email=?";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'ss',$user_name,$user_email);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);
$ret=array();

if(!$row){
    
    $mysql_qry = "insert into users(user_username,user_password,user_status_id ,user_email) values (?,?,'1',?)";
    // $result = mysqli_query($conn ,$mysql_qry);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'sss',$user_name,$user_pass,$user_email);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    
    $userid = mysqli_insert_id($conn);
    $token=bin2hex(openssl_random_pseudo_bytes(32));
    $mysql_qry2 = "insert into tokens(token_body,user_id) values(?,?)";
    // $result2 = mysqli_query($conn ,$mysql_qry2);
    $result2 = mysqli_prepare($conn ,$mysql_qry2);
    mysqli_stmt_bind_param($result2,'ss',$token,$userid);
    mysqli_stmt_execute($result2);
    $result2= mysqli_stmt_get_result($result2);
    
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