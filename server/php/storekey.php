<?php
include("db_con.php");
include("token.php");
header('Content-type:application/json');
$encrypt_PK = $_REQUEST["encryptpk"];
$user_publickey = $_REQUEST["publickey"];
 

// $mysql_qry = "insert into store_key(privatekey,user_id) values (?,?)";
// $result = mysqli_prepare($conn ,$mysql_qry);
// mysqli_stmt_bind_param($result,'ss',$encrypt_PK,$user_id);
// mysqli_stmt_execute($result);
// $result= mysqli_stmt_get_result($result);

$mysql_qry = "update users set user_publickey=?,user_privatekey=? where user_id=?";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'sss',$user_publickey,$encrypt_PK,$user_id);
mysqli_stmt_execute($result);
$ret['status']="success";
echo json_encode($ret);


?>