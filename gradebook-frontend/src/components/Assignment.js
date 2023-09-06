import React from 'react';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { Link } from 'react-router-dom'
import Cookies from 'js-cookie';
import Button from '@mui/material/Button';
import ButtonGroup from '@mui/material/ButtonGroup';
import Radio from '@mui/material/Radio';
import Grid from '@mui/material/Grid';
import {DataGrid} from '@mui/x-data-grid';
import {SERVER_URL} from '../constants.js'
import AddAssignment from './AddAssignment';

// NOTE:  for OAuth security, http request must have
//   credentials: 'include' 
//

class Assignment extends React.Component {
    constructor(props) {
      super(props);
      this.state = {selected: 0, assignments: []};
    };
 
   componentDidMount() {
    this.fetchAssignments();
  }
 
  fetchAssignments = () => {
    console.log("Assignment.fetchAssignments");
    const token = Cookies.get('XSRF-TOKEN');
    fetch(`${SERVER_URL}/gradebook`, 
      {  
        method: 'GET', 
        headers: { 'X-XSRF-TOKEN': token }
      } )
    .then((response) => response.json()) 
    .then((responseData) => { 
      if (Array.isArray(responseData.assignments)) {
        //  add to each assignment an "id"  This is required by DataGrid  "id" is the row index in the data grid table 
        this.setState({ assignments: responseData.assignments.map((assignment, index) => ( { id: index, ...assignment } )) });
      } else {
        toast.error("Fetch failed.", {
          position: toast.POSITION.BOTTOM_LEFT
        });
      }        
    })
    .catch(err => console.error(err)); 
  }
  
   onRadioClick = (event) => {
    console.log("Assignment.onRadioClick " + event.target.value);
    this.setState({selected: event.target.value});
  }
  
    // Add assignment
	addAssignment = (assignment) => {
		const token = Cookies.get('XSRF-TOKEN');
		
		// console.log(JSON.stringify(assignment));
		
		fetch(`${SERVER_URL}/assignment`,
		  { 
			method: 'POST', 
			headers: { 'Content-Type': 'application/json',
					   'X-XSRF-TOKEN': token  }, 
			body: JSON.stringify(assignment)
		  })
		.then(res => {
			if (res.ok) {
			  toast.success("Assignment successfully added", {
				  position: toast.POSITION.BOTTOM_LEFT
			  });
			  this.fetchAssignments();
			} else {
			  toast.error("Error when adding", {
				  position: toast.POSITION.BOTTOM_LEFT
			  });
			  console.error('Post http status =' + res.status);
			}})
		.catch(err => {
		  toast.error("Error when adding", {
				position: toast.POSITION.BOTTOM_LEFT
			});
			console.error(err);
		})
	  }
  
  render() {
     const columns = [
      {
        field: 'assignmentName',
        headerName: 'Assignment',
        width: 400,
        renderCell: (params) => (
          <div>
          <Radio
            checked={params.row.id == this.state.selected}
            onChange={this.onRadioClick}
            value={params.row.id}
            color="default"
            size="small"
          />
          {params.value}
          </div>
        )
      },
      { field: 'courseTitle', headerName: 'Course', width: 300 },
      { field: 'dueDate', headerName: 'Due Date', width: 200 }
      ];
      
      const assignmentSelected = this.state.assignments[this.state.selected];
      return (
          <div align="left" >
			<div>
			<Grid container>
              <Grid item>
				<ButtonGroup>
					<AddAssignment addAssignment={this.addAssignment}  />
				</ButtonGroup>
              </Grid>
            </Grid>
			</div>
            <h4>Assignment(s) ready to grade: </h4>
              <div style={{ height: 450, width: '100%', align:"left"   }}>
                <DataGrid rows={this.state.assignments} columns={columns} />
              </div>                
            <Button component={Link} to={{pathname:'/gradebook',   assignment: assignmentSelected }} 
                    variant="outlined" color="primary" disabled={this.state.assignments.length===0}  style={{margin: 10}}>
              Grade
            </Button>
            <ToastContainer autoClose={1500} /> 
          </div>
      )
  }
}  

export default Assignment;