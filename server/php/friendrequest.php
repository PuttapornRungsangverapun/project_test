<?php
include("db_con.php");
include('token.php');
header('Content-type:application/json');

$mysql_qry="SELECT friends.user_id FROM friends WHERE friends.user_id=? or friends.friend_id=?";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'ss',$user_id,$user_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
//$row=mysqli_fetch_array($result);
//$row=mysqli_fetch_all($result);



$lvname=array();
while($row=mysqli_fetch_assoc($result)){
    
 

$mysql_qry2="SELECT friends.user_id FROM friends WHERE (friends.user_id=? and friends.friend_id=?) or (friends.user_id=? and friends.friend_id=?)";
$result2 = mysqli_prepare($conn ,$mysql_qry2);
mysqli_stmt_bind_param($result2,'ssss',$user_id,$row['user_id'],$row['user_id'],$user_id);
mysqli_stmt_execute($result2);
$result2= mysqli_stmt_get_result($result2);
while($row2=mysqli_fetch_assoc($result2)){
if(mysqli_num_rows($result2)==1){

$mysql_qry3="SELECT users.user_id,users.user_username FROM users WHERE  users.user_id=?";
$result3 = mysqli_prepare($conn ,$mysql_qry3);
mysqli_stmt_bind_param($result3,'s',$row2['user_id']);
mysqli_stmt_execute($result3);
$result3= mysqli_stmt_get_result($result3);

    while($row3=mysqli_fetch_assoc($result3)){
       $lvname[]=$row3;
    }
   
}

}  
}
$ret['status']="success";
$ret['message']=$lvname;

echo json_encode($ret);



?>