import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';


// properties addAssignment is required, function called when Add clicked.
class AddAssignment extends Component {
      constructor(props) {
      super(props);
      this.state = {open: false, assignment:{ } };
    };
    
    handleClickOpen = () => {
      this.setState( {open:true} );
    };

    handleClose = () => {
      this.setState( {open:false} );
    };

    handleChangeAssignmentName = (event) => {
	  this.setState(prevState => ({assignment:{assignmentName: event.target.value, dueDate: prevState.assignment.dueDate, courseId: prevState.assignment.courseId}}))
    }
	
	handleChangeAssignmentDueDate = (event) => {
	  this.setState(prevState => ({assignment:{assignmentName: prevState.assignment.assignmentName, dueDate: event.target.value, courseId: prevState.assignment.courseId}}))
    }
	
	handleChangeAssignmentCourseId = (event) => {
	  this.setState(prevState => ({assignment:{assignmentName: prevState.assignment.assignmentName, dueDate: prevState.assignment.dueDate, courseId: parseInt(event.target.value)}}))
    }

  // Save assignment and close modal form
    handleAdd = () => {
       this.props.addAssignment(this.state.assignment);
       this.handleClose();
    }

    render()  { 
      return (
          <div>
            <Button variant="outlined" color="primary" style={{margin: 10}} onClick={this.handleClickOpen}>
              Add Assignment
            </Button>
            <Dialog open={this.state.open} onClose={this.handleClose}>
                <DialogTitle>Add Assignment</DialogTitle>
                <DialogContent  style={{paddingTop: 20}} >
                  <TextField autoFocus fullWidth label="Assignment Name" name="assignmentName" onChange={this.handleChangeAssignmentName}  /> 
                </DialogContent>
				<DialogContent  style={{paddingTop: 20}} >
                  <TextField autoFocus fullWidth label="Due Date" name="dueDate" onChange={this.handleChangeAssignmentDueDate}  /> 
                </DialogContent>
				<DialogContent  style={{paddingTop: 20}} >
                  <TextField autoFocus fullWidth label="Course Id" name="courseId" onChange={this.handleChangeAssignmentCourseId}  /> 
                </DialogContent>
                <DialogActions>
                  <Button color="secondary" onClick={this.handleClose}>Cancel</Button>
                  <Button id="Add" color="primary" onClick={this.handleAdd}>Add</Button>
                </DialogActions>
              </Dialog>      
          </div>
      ); 
    }
}

// required property:  addAssignment is a function to call to perform the Add action
AddAssignment.propTypes = {
  addAssignment : PropTypes.func.isRequired
}

export default AddAssignment;