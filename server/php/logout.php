<?php
include('db_con.php');
include('token.php');
header('Content-type:application/json');
$password = sha1($_REQUEST["password"]);
$encryptpk =$_REQUEST["encryptpk"];

$mysql_qry="select users.user_id from users where users.user_password=? and users.user_id=?";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'ss',$password,$user_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);

if($row){
    $mysql_qry = "insert into log_logout(privatekey,user_id) values (?,?)";
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$encryptpk,$user_id);
    $row=mysqli_stmt_execute($result);
    $ret['status']="success";
    $ret['message']="Logout success";
}else{
    $ret['status']="fail";
    $ret['message']="Logout fail";
}
?>